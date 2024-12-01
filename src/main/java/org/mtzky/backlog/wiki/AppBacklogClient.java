package org.mtzky.backlog.wiki;

import com.nulabinc.backlog4j.BacklogClient;
import com.nulabinc.backlog4j.BacklogClientFactory;
import com.nulabinc.backlog4j.BacklogException;
import com.nulabinc.backlog4j.Wiki;
import com.nulabinc.backlog4j.api.option.GetParams;
import com.nulabinc.backlog4j.api.option.QueryParams;
import com.nulabinc.backlog4j.api.option.UpdateWikiParams;
import com.nulabinc.backlog4j.http.BacklogHttpResponse;
import com.nulabinc.backlog4j.http.NameValuePair;
import com.nulabinc.backlog4j.http.httpclient.HttpClientBacklogHttpClient;

import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.WARNING;

public class AppBacklogClient {

    private final String projectIdOrKey;
    private final BacklogClient client;

    public AppBacklogClient(final AppConfig config) throws MalformedURLException {
        projectIdOrKey = config.getProjectIdOrKey();
        final var configure = config.getSpaceType()
                .newBacklogConfigure(config.getSpaceId())
                .apiKey(config.getApiKey());
        final var httpClient = new AppBacklogHttpClient();
        client = new BacklogClientFactory(configure, httpClient).newClient();
    }

    public List<Wiki> getWikis() {
        return client.getWikis(projectIdOrKey);
    }

    public Wiki getWiki(final long id) {
        return client.getWiki(id);
    }

    public Wiki updateWiki(final UpdateWikiParams params) {
        return client.updateWiki(params);
    }

    private static class AppBacklogHttpClient extends HttpClientBacklogHttpClient {

        private static final System.Logger LOG = System.getLogger(AppBacklogHttpClient.class.getName());
        private static final int MAX_ATTEMPTS = 3;

        @Override
        public BacklogHttpResponse get(
                final String endpoint,
                final GetParams getParams,
                final QueryParams queryParams) throws BacklogException {
            return attempt(endpoint, () -> super.get(endpoint, getParams, queryParams));
        }

        @Override
        public BacklogHttpResponse post(
                final String endpoint,
                final List<NameValuePair> postParams,
                final List<NameValuePair> headers) throws BacklogException {
            return attempt(endpoint, () -> super.post(endpoint, postParams, headers));
        }

        @Override
        public BacklogHttpResponse patch(
                final String endpoint,
                final List<NameValuePair> patchParams,
                final List<NameValuePair> headers) throws BacklogException {
            return attempt(endpoint, () -> super.patch(endpoint, patchParams, headers));
        }

        @Override
        public BacklogHttpResponse put(
                final String endpoint,
                final List<NameValuePair> patchParams) throws BacklogException {
            return attempt(endpoint, () -> super.put(endpoint, patchParams));
        }

        @Override
        public BacklogHttpResponse delete(
                final String endpoint,
                final List<NameValuePair> deleteParams) throws BacklogException {
            return attempt(endpoint, () -> super.delete(endpoint, deleteParams));
        }

        @Override
        public BacklogHttpResponse postMultiPart(
                final String endpoint,
                final Map<String, Object> postParams) throws BacklogException {
            return attempt(endpoint, () -> super.postMultiPart(endpoint, postParams));
        }

        private BacklogHttpResponse attempt(final String endpoint, final Supplier<BacklogHttpResponse> request) {
            BacklogHttpResponse response = null;
            for (var i = 0; i < MAX_ATTEMPTS; i++) {
                response = request.get();
                final var resetDate = response.getRateLimitResetDate();

                LOG.log(
                        DEBUG,
                        "url={0}, count={1}, statusCode={2}, rateLimit={3}/{4} ({5,date,yyyy-MM-dd HH:mm:ss zzz})",
                        endpoint,
                        i,
                        response.getStatusCode(),
                        response.getRateLimitRemaining(),
                        response.getRateLimitLimit(),
                        resetDate
                );

                if (response.getStatusCode() != 429) {
                    return response;
                }

                final var sleepMillis = resetDate.getTime() - System.currentTimeMillis();
                try {
                    Thread.sleep(sleepMillis);
                } catch (final InterruptedException e) {
                    LOG.log(WARNING, "Failed to sleep", e);
                    break;
                }
            }

            return response;
        }

    }

}

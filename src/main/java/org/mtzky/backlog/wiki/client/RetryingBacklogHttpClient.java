package org.mtzky.backlog.wiki.client;

import com.nulabinc.backlog4j.BacklogException;
import com.nulabinc.backlog4j.api.option.GetParams;
import com.nulabinc.backlog4j.api.option.QueryParams;
import com.nulabinc.backlog4j.http.BacklogHttpResponse;
import com.nulabinc.backlog4j.http.NameValuePair;
import com.nulabinc.backlog4j.http.httpclient.HttpClientBacklogHttpClient;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.WARNING;

class RetryingBacklogHttpClient extends HttpClientBacklogHttpClient {

    private static final System.Logger LOG = System.getLogger(RetryingBacklogHttpClient.class.getName());
    private static final int MAX_ATTEMPTS = 3;

    private static BacklogHttpResponse attempt(final String endpoint, final Supplier<BacklogHttpResponse> request) {
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

}

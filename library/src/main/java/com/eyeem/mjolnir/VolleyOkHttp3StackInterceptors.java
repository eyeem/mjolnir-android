package com.eyeem.mjolnir;
/**
 * SOURCE: https://gist.github.com/LOG-TAG/3ad1c191b3ca7eab3ea6834386e30eb9
 *
 * Created by @subrahmanya  on 2/3/18.
 * CREDITS:
 * <1>https://gist.github.com/alashow/c96c09320899e4caa06b
 * <2>https://gist.github.com/intari/e57a945eed9c2ee0f9eb9082469698f3
 * <3>https://gist.github.com/alirezaafkar/a62d6a9a7e582322ca1a764bad116a70
 *
 * 
 * Reason: for making the Volley use latest okhttpstack work for latest version Volley 1.1.0 by removing all deprecated org.apache dependencies! 
 */

import com.android.volley.AuthFailureError;
import com.android.volley.Header;
import com.android.volley.Request;
import com.android.volley.toolbox.BaseHttpStack;
import com.android.volley.toolbox.HttpResponse;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;


public class VolleyOkHttp3StackInterceptors extends BaseHttpStack {
    private final List<Interceptor> mInterceptors;

    public VolleyOkHttp3StackInterceptors(List<Interceptor> interceptors) {
        this.mInterceptors = interceptors;
    }

    public static VolleyOkHttp3StackInterceptors newInstance() {
        return new VolleyOkHttp3StackInterceptors(new ArrayList<Interceptor>());
    }

    private static void setConnectionParametersForRequest(okhttp3.Request.Builder builder, Request<?> request)
            throws AuthFailureError {
        switch (request.getMethod()) {
            case Request.Method.DEPRECATED_GET_OR_POST:
                // Ensure backwards compatibility.  Volley assumes a request with a null body is a GET.
                byte[] postBody = request.getBody();
                if (postBody != null) {
                    builder.post(RequestBody.create(MediaType.parse(request.getBodyContentType()), postBody));
                }
                break;
            case Request.Method.GET:
                builder.get();
                break;
            case Request.Method.DELETE:
                builder.delete(createRequestBody(request));
                break;
            case Request.Method.POST:
                builder.post(createRequestBody(request));
                break;
            case Request.Method.PUT:
                builder.put(createRequestBody(request));
                break;
            case Request.Method.HEAD:
                builder.head();
                break;
            case Request.Method.OPTIONS:
                builder.method("OPTIONS", null);
                break;
            case Request.Method.TRACE:
                builder.method("TRACE", null);
                break;
            case Request.Method.PATCH:
                builder.patch(createRequestBody(request));
                break;
            default:
                throw new IllegalStateException("Unknown method type.");
        }
    }

    private static RequestBody createRequestBody(Request r) throws AuthFailureError {
        final byte[] body = r.getBody();
        if (body == null) {
            return null;
        }
        return RequestBody.create(MediaType.parse(r.getBodyContentType()), body);
    }

    @Override
    public HttpResponse executeRequest(Request<?> request, Map<String, String> additionalHeaders) throws IOException, AuthFailureError {
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
        int timeoutMs = request.getTimeoutMs();

        clientBuilder.connectTimeout(timeoutMs, TimeUnit.MILLISECONDS);
        clientBuilder.readTimeout(timeoutMs, TimeUnit.MILLISECONDS);
        clientBuilder.writeTimeout(timeoutMs, TimeUnit.MILLISECONDS);

        okhttp3.Request.Builder okHttpRequestBuilder = new okhttp3.Request.Builder();
        okHttpRequestBuilder.url(request.getUrl());

        Headers headers = new Headers.Builder()
           .addAll(Headers.of(request.getHeaders()))
           .addAll(Headers.of(additionalHeaders))
           .build();
        okHttpRequestBuilder.headers(headers);

        setConnectionParametersForRequest(okHttpRequestBuilder, request);


        for (Interceptor interceptor : mInterceptors) {
            clientBuilder.addNetworkInterceptor(interceptor);
        }

        OkHttpClient client = clientBuilder.build();
        okhttp3.Request okHttpRequest = okHttpRequestBuilder.build();
        Call okHttpCall = client.newCall(okHttpRequest);
        Response okHttpResponse = okHttpCall.execute();


        int code = okHttpResponse.code();
        ResponseBody body = okHttpResponse.body();
        InputStream content = body == null ? null : body.byteStream();
        int contentLength = body == null ? 0 : (int) body.contentLength();
        List<Header> responseHeaders = mapHeaders(okHttpResponse.headers());
        return new HttpResponse(code, responseHeaders, contentLength, content);
    }

    private List<Header> mapHeaders(Headers responseHeaders) {
        List<Header> headers = new ArrayList<>();
        for (int i = 0, len = responseHeaders.size(); i < len; i++) {
            final String name = responseHeaders.name(i), value = responseHeaders.value(i);
            if (name != null) {
                headers.add(new Header(name, value));
            }
        }
        return headers;
    }
}


package com.kkambbak.global.config;

import org.slf4j.MDC;
import org.zalando.logbook.HttpLogFormatter;
import org.zalando.logbook.Precorrelation;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.json.JsonHttpLogFormatter;

import java.io.IOException;

public class CustomJsonHttpLogFormatter implements HttpLogFormatter {
    private static final String MDC_METHOD = "logbook.method";
    private static final String MDC_PATH = "logbook.path";
    private static final String MDC_QUERY = "logbook.query";
    private static final String MDC_TRACE_ID = "traceId";

    private final JsonHttpLogFormatter delegate = new JsonHttpLogFormatter();

    @Override
    public String format(Precorrelation precorrelation, HttpRequest request) throws IOException {
        try {
            MDC.put(MDC_METHOD, request.getMethod());
            MDC.put(MDC_PATH, request.getPath());
            MDC.put(MDC_QUERY, request.getQuery());

            String traceId = MDC.get(MDC_TRACE_ID);
            if (traceId == null) {
                traceId = precorrelation.getId();
            }

            String message = String.format("[REQUEST] %s %s%s [%s]",
                    request.getMethod(),
                    request.getPath(),
                    request.getQuery().isEmpty() ? "" : "?" + request.getQuery(),
                    traceId);

            String json = delegate.format(precorrelation, request);
            return String.format("{\"level\":\"INFO\",\"message\":%s,%s", escape(message), json.substring(1));
        } catch (Exception e) {
            clearMDC();
            throw e;
        }
    }

    @Override
    public String format(Correlation correlation, HttpResponse response) throws IOException {
        try {
            String path = MDC.get(MDC_PATH);
            String query = MDC.get(MDC_QUERY);
            String traceId = MDC.get(MDC_TRACE_ID);

            String message = String.format("[RESPONSE] %d %s%s [%s]",
                    response.getStatus(),
                    path != null ? path : "UNKNOWN",
                    (query != null && !query.isEmpty()) ? "?" + query : "",
                    traceId != null ? traceId : correlation.getId());

            String json = delegate.format(correlation, response);

            String level;
            int status = response.getStatus();
            if (status >= 500) {
                level = "ERROR";
            } else if (status >= 400) {
                level = "WARN";
            } else {
                level = "INFO";
            }

            return String.format("{\"level\":\"%s\",\"message\":%s,%s", level, escape(message), json.substring(1));
        } finally {
            MDC.clear();
        }
    }

    private void clearMDC() {
        MDC.remove(MDC_METHOD);
        MDC.remove(MDC_PATH);
        MDC.remove(MDC_QUERY);
        MDC.remove(MDC_TRACE_ID);
    }

    private String escape(String s) {
        return "\"" + s.replace("\"", "\\\"") + "\"";
    }
}
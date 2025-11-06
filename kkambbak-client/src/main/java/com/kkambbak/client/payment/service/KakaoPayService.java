package com.kkambbak.client.payment.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoPayService {

    private final RestTemplate restTemplate;

    @Value("${kakao.pay.client-id:test_client_id}")
    private String kakaoClientId;

    @Value("${kakao.pay.client-secret:test_client_secret}")
    private String kakaoClientSecret;

    @Value("${kakao.pay.secret-key:test_secret_key}")
    private String kakaoSecretKey;

    @Value("${kakao.pay.cid:test_cid}")
    private String cid;

    @Value("${kakao.pay.subscription-cid:test_cid}")
    private String subscriptionCid;

    @Value("${kakao.pay.api-url:https://test.kakao.com}")
    private String kakaoPayApiUrl;

    @Value("${kakao.pay.callback.approval-url:http://localhost:8080/api/v1/payments/approve}")
    private String approvalUrl;

    @Getter
    @Value("${kakao.pay.callback.success-url:http://localhost:3000/payment/success}")
    private String successUrl;

    @Getter
    @Value("${kakao.pay.callback.cancel-url:http://localhost:3000/payment/fail}")
    private String cancelUrl;

    @Getter
    @Value("${kakao.pay.callback.fail-url:http://localhost:3000/payment/fail}")
    private String failUrl;

    /**
     * 카카오페이 결제 준비
     */
    public Map<String, Object> readyPayment(String partnerOrderId, String partnerUserId,
                                            String itemName, Integer quantity, Integer totalAmount,
                                            Integer taxFreeAmount) throws IOException {
        Map<String, Object> requestBody = createReadyRequest(cid, partnerOrderId, partnerUserId,
                itemName, quantity, totalAmount, taxFreeAmount);

        String url = kakaoPayApiUrl + "/v1/payment/ready";
        log.info("Ready payment - partnerOrderId: {}, itemName: {}, totalAmount: {}",
                partnerOrderId, itemName, totalAmount);

        Map<String, Object> response = makePostRequest(url, requestBody);

        log.info("Payment ready completed - partnerOrderId: {}, tid: {}",
                partnerOrderId, response != null ? response.get("tid") : null);
        return response;
    }

    /**
     * 카카오페이 결제 승인
     */
    public Map<String, Object> approvePayment(String tid, String partnerOrderId,
                                              String partnerUserId, String pgToken, boolean autoRenew) throws IOException {
        String cidToUse = autoRenew ? subscriptionCid : cid;

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("cid", cidToUse);
        requestBody.put("tid", tid);
        requestBody.put("partner_order_id", partnerOrderId);
        requestBody.put("partner_user_id", partnerUserId);
        requestBody.put("pg_token", pgToken);

        String url = kakaoPayApiUrl + "/v1/payment/approve";

        Map<String, Object> response = makePostRequest(url, requestBody);

        log.info("Payment approved - tid: {}, partnerOrderId: {}", tid, partnerOrderId);
        return response;
    }

    /**
     * 카카오페이 결제 조회
     */
    public Map<String, Object> getPaymentOrder(String tid) throws IOException {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("cid", cid);
        requestBody.put("tid", tid);

        String url = kakaoPayApiUrl + "/v1/payment/order";
        log.info("Getting payment order - tid: {}", tid);

        Map<String, Object> response = makePostRequest(url, requestBody);

        log.info("Payment order retrieved - tid: {}", tid);
        return response;
    }

    /**
     * 카카오페이 결제 취소
     */
    public Map<String, Object> cancelPayment(String tid, Integer cancelAmount,
                                             Integer cancelTaxFreeAmount) throws IOException {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("cid", cid);
        requestBody.put("tid", tid);
        requestBody.put("cancel_amount", cancelAmount);
        requestBody.put("cancel_tax_free_amount", cancelTaxFreeAmount);

        String url = kakaoPayApiUrl + "/v1/payment/cancel";
        log.info("Canceling payment - tid: {}, cancelAmount: {}", tid, cancelAmount);

        Map<String, Object> response = makePostRequest(url, requestBody);

        log.info("Payment canceled - tid: {}", tid);
        return response;
    }

    /**
     * 카카오페이 정기결제 준비 (1회차)
     */
    public Map<String, Object> readySubscription(String partnerOrderId, String partnerUserId,
                                                 String itemName, Integer quantity, Integer totalAmount,
                                                 Integer taxFreeAmount) throws IOException {
        Map<String, Object> requestBody = createReadyRequest(subscriptionCid, partnerOrderId, partnerUserId,
                itemName, quantity, totalAmount, taxFreeAmount);

        String url = kakaoPayApiUrl + "/v1/payment/ready";
        log.info("Ready subscription - partnerOrderId: {}, itemName: {}, totalAmount: {}",
                partnerOrderId, itemName, totalAmount);

        Map<String, Object> response = makePostRequest(url, requestBody);

        log.info("Subscription ready completed - partnerOrderId: {}, sid: {}",
                partnerOrderId, response != null ? response.get("sid") : null);
        return response;
    }

    /**
     * 카카오페이 정기결제 승인
     */
    public Map<String, Object> payWithSubscription(String sid, String partnerOrderId,
                                                   String partnerUserId, String itemName,
                                                   Integer quantity, Integer totalAmount,
                                                   Integer taxFreeAmount) throws IOException {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("cid", subscriptionCid);
        requestBody.put("sid", sid);
        requestBody.put("partner_order_id", partnerOrderId);
        requestBody.put("partner_user_id", partnerUserId);
        requestBody.put("item_name", itemName);
        requestBody.put("quantity", quantity);
        requestBody.put("total_amount", totalAmount);
        requestBody.put("tax_free_amount", taxFreeAmount);

        String url = kakaoPayApiUrl + "/v1/payment/subscription";
        log.info("Processing subscription payment - sid: {}, partnerOrderId: {}, totalAmount: {}",
                sid, partnerOrderId, totalAmount);

        Map<String, Object> response = makePostRequest(url, requestBody);

        log.info("Subscription payment processed - partnerOrderId: {}", partnerOrderId);
        return response;
    }

    /**
     * 결제 준비 요청 바디 생성
     */
    private Map<String, Object> createReadyRequest(String cidToUse, String partnerOrderId, String partnerUserId,
                                                   String itemName, Integer quantity, Integer totalAmount,
                                                   Integer taxFreeAmount) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("cid", cidToUse);
        requestBody.put("partner_order_id", partnerOrderId);
        requestBody.put("partner_user_id", partnerUserId);
        requestBody.put("item_name", itemName);
        requestBody.put("quantity", quantity);
        requestBody.put("total_amount", totalAmount);
        requestBody.put("tax_free_amount", taxFreeAmount);
        requestBody.put("approval_url", this.approvalUrl + "?orderId=" + partnerOrderId);
        requestBody.put("cancel_url", this.cancelUrl);
        requestBody.put("fail_url", this.failUrl);
        return requestBody;
    }

    /**
     * 공통 POST 요청 메서드
     */
    private Map<String, Object> makePostRequest(String url, Map<String, Object> requestBody) throws IOException {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "SECRET_KEY " + kakaoSecretKey);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            return restTemplate.exchange(url, HttpMethod.POST, request, new ParameterizedTypeReference<Map<String, Object>>() {})
                .getBody();

        } catch (Exception e) {
            String errorMessage = extractKakaoPayErrorMessage(e);
            log.error("Failed to make POST request to: {} - {}", url, errorMessage, e);
            throw new IOException(errorMessage, e);
        }
    }

    /**
     * 정기결제 비활성화 (구독 취소)
     */
    public void inactiveSubscription(String sid) throws IOException {
        String url = kakaoPayApiUrl + "/online/v1/payment/manage/subscription/inactive";

        Map<String, Object> params = new HashMap<>();
        params.put("cid", subscriptionCid);
        params.put("sid", sid);

        makePostRequest(url, params);
    }

    /**
     * KakaoPay 에러 응답에서 상세 메시지 추출
     */
    private String extractKakaoPayErrorMessage(Exception e) {
        if (e instanceof HttpClientErrorException httpEx) {
            try {
                String responseBody = httpEx.getResponseBodyAsString();
                Map<String, Object> errorResponse = new ObjectMapper()
                    .readValue(responseBody, new TypeReference<>() {});

                String errorMessage = (String) errorResponse.get("error_message");
                Integer errorCode = (Integer) errorResponse.get("error_code");

                StringBuilder sb = new StringBuilder();
                sb.append("[KakaoPay Error] ");
                if (errorCode != null) {
                    sb.append("Code: ").append(errorCode).append(" ");
                }
                if (errorMessage != null) {
                    sb.append("Message: ").append(errorMessage);
                }

                Object extrasObj = errorResponse.get("extras");
                if (extrasObj instanceof Map<?, ?> extras) {
                    Object methodResult = extras.get("method_result_message");
                    if (methodResult != null) {
                        sb.append(" (").append(methodResult).append(")");
                    }
                }

                return sb.toString();
            } catch (Exception parseEx) {
                log.debug("Failed to parse KakaoPay error response", parseEx);
                return "KakaoPay API 요청 실패: " + e.getMessage();
            }
        }
        return "API 요청 실패: " + e.getMessage();
    }

}
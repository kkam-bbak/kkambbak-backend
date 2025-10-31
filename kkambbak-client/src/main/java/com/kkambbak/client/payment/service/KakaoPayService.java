package com.kkambbak.client.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoPayService {

    private final RestTemplate restTemplate;

    @Value("${kakao.pay.admin-key:test_admin_key}")
    private String kakaoAdminKey;

    @Value("${kakao.pay.cid:test_cid}")
    private String cid;

    @Value("${kakao.pay.api-url:https://kapi.kakao.com}")
    private String kakaoPayApiUrl;

    /**
     * 카카오페이 결제 준비
     */
    public Map<String, Object> readyPayment(String partnerOrderId, String partnerUserId,
                                            String itemName, Integer quantity, Integer totalAmount,
                                            Integer taxFreeAmount, String approvalUrl,
                                            String cancelUrl, String failUrl) throws IOException {
        Map<String, Object> requestBody = createReadyRequest(partnerOrderId, partnerUserId,
                itemName, quantity, totalAmount, taxFreeAmount, approvalUrl, cancelUrl, failUrl);

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
                                              String partnerUserId, String pgToken) throws IOException {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("cid", cid);
        requestBody.put("tid", tid);
        requestBody.put("partner_order_id", partnerOrderId);
        requestBody.put("partner_user_id", partnerUserId);
        requestBody.put("pg_token", pgToken);

        String url = kakaoPayApiUrl + "/v1/payment/approve";
        log.info("Approving payment - tid: {}, partnerOrderId: {}", tid, partnerOrderId);

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
     * 카카오페이 정기결제 준비
     */
    public Map<String, Object> readySubscription(String partnerOrderId, String partnerUserId,
                                                 String itemName, Integer quantity, Integer totalAmount,
                                                 Integer taxFreeAmount, String approvalUrl,
                                                 String cancelUrl, String failUrl) throws IOException {
        Map<String, Object> requestBody = createReadyRequest(partnerOrderId, partnerUserId,
                itemName, quantity, totalAmount, taxFreeAmount, approvalUrl, cancelUrl, failUrl);

        String url = kakaoPayApiUrl + "/v1/payment/subscription";
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
        requestBody.put("cid", cid);
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
    private Map<String, Object> createReadyRequest(String partnerOrderId, String partnerUserId,
                                                   String itemName, Integer quantity, Integer totalAmount,
                                                   Integer taxFreeAmount, String approvalUrl,
                                                   String cancelUrl, String failUrl) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("cid", cid);
        requestBody.put("partner_order_id", partnerOrderId);
        requestBody.put("partner_user_id", partnerUserId);
        requestBody.put("item_name", itemName);
        requestBody.put("quantity", quantity);
        requestBody.put("total_amount", totalAmount);
        requestBody.put("tax_free_amount", taxFreeAmount);
        requestBody.put("approval_url", approvalUrl);
        requestBody.put("cancel_url", cancelUrl);
        requestBody.put("fail_url", failUrl);
        return requestBody;
    }

    /**
     * 공통 POST 요청 메서드
     */
    private Map<String, Object> makePostRequest(String url, Map<String, Object> requestBody) throws IOException {
        try {
            HttpHeaders headers = getHeaders();
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(url, request, Map.class);

            return response;

        } catch (Exception e) {
            log.error("Failed to make POST request to: {}", url, e);
            throw new IOException("API request failed", e);
        }
    }

    /**
     * 카카오페이 API 헤더 생성
     */
    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + kakaoAdminKey);
        headers.set("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");

        return headers;
    }
}
package com.kkambbak.domain.payment.controller;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.kkambbak.KkambbakDocumentApiTester;
import com.kkambbak.domain.payment.dto.PaymentDto;
import org.junit.jupiter.api.Test;
import org.springframework.restdocs.payload.JsonFieldType;

import java.util.Map;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PaymentControllerTest extends KkambbakDocumentApiTester {

    @Test
    void createPaymentTest() throws Exception {
        // given
        PaymentDto.CreateResponse mockResponse = PaymentDto.CreateResponse.builder()
                .paymentId(1L)
                .orderId("order_1_1234567890")
                .approvalUrl("https://open-api.kakaopay.com/online/web/next_redirect_pc_url")
                .build();
        given(paymentFacade.createPayment(anyLong(), anyLong())).willReturn(mockResponse);

        // when & then
        this.mockMvc.perform(post("/api/v1/payments/create/1")
                        .header("Authorization", "Bearer access_token_example")
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andDo(document("payment-create",
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("Payments")
                                        .summary("결제 생성")
                                        .description("구독 플랜을 선택하여 결제를 준비합니다. 카카오페이 승인 URL을 반환받습니다.")
                                        .requestHeaders(
                                                headerWithName("Authorization").description("Bearer 토큰")
                                        )
                                        .responseFields(
                                                fieldWithPath("status.statusCode").type(JsonFieldType.STRING).description("상태 코드"),
                                                fieldWithPath("status.message").type(JsonFieldType.STRING).description("상태 메시지"),
                                                fieldWithPath("status.description").type(JsonFieldType.STRING).description("상태 설명").optional(),
                                                fieldWithPath("body").type(JsonFieldType.OBJECT).description("응답 데이터"),
                                                fieldWithPath("body.paymentId").type(JsonFieldType.NUMBER).description("결제 기록 ID"),
                                                fieldWithPath("body.orderId").type(JsonFieldType.STRING).description("주문 ID"),
                                                fieldWithPath("body.approvalUrl").type(JsonFieldType.STRING).description("카카오페이 승인 URL")
                                        )
                                        .build()
                        )
                ));
    }

    @Test
    void capturePaymentTest() throws Exception {
        // given
        doNothing().when(paymentFacade).capturePayment(anyLong(), anyLong(), any(String.class), any(String.class));

        // when & then
        this.mockMvc.perform(post("/api/v1/payments/capture")
                        .header("Authorization", "Bearer access_token_example")
                        .contentType("application/json")
                        .content(toJson(Map.of(
                                "paymentId", 1L,
                                "orderId", "order_1_1234567890",
                                "pg_token", "05158049924b3495c98b"
                        ))))
                .andExpect(status().isOk())
                .andDo(document("payment-capture",
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("Payments")
                                        .summary("결제 승인")
                                        .description("카카오페이에서 사용자 승인 후 받은 (리다이액트 url에 포함되어있음) pg_token, 결제 생성 때 응답에 나온 정보들로 결제를 승인하고 구독을 생성합니다.")
                                        .requestHeaders(
                                                headerWithName("Authorization").description("Bearer 토큰")
                                        )
                                        .requestFields(
                                                fieldWithPath("paymentId").type(JsonFieldType.NUMBER).description("결제 기록 ID"),
                                                fieldWithPath("orderId").type(JsonFieldType.STRING).description("주문 ID"),
                                                fieldWithPath("pg_token").type(JsonFieldType.STRING).description("카카오페이 승인 토큰 (리다이렉트 URL의 pg_token 파라미터)")
                                        )
                                        .responseFields(
                                                fieldWithPath("status.statusCode").type(JsonFieldType.STRING).description("상태 코드"),
                                                fieldWithPath("status.message").type(JsonFieldType.STRING).description("상태 메시지"),
                                                fieldWithPath("status.description").type(JsonFieldType.STRING).description("상태 설명").optional()
                                        )
                                        .build()
                        )
                ));
    }
}
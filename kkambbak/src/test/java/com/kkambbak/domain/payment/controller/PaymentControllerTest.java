package com.kkambbak.domain.payment.controller;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.kkambbak.KkambbakDocumentApiTester;
import com.kkambbak.domain.payment.dto.PaymentDetailDto;
import com.kkambbak.domain.payment.dto.PaymentDto;
import com.kkambbak.domain.payment.dto.PaymentResultDto;
import com.kkambbak.domain.payment.facade.PaymentFacade;
import com.kkambbak.domain.payment.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
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
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PaymentControllerTest extends KkambbakDocumentApiTester {

    @MockitoBean
    private PaymentFacade paymentFacade;

    @MockitoBean
    private PaymentService paymentService;

    @Test
    void createPaymentTest() throws Exception {
        // given
        PaymentDto.CreateResponse mockResponse = PaymentDto.CreateResponse.builder()
                .paymentId(1L)
                .orderId("order_1_1234567890")
                .approvalUrl("https://open-api.kakaopay.com/online/web/next_redirect_pc_url")
                .build();
        given(paymentFacade.createPayment(anyLong(), anyLong(), any(PaymentDto.CreateRequest.class))).willReturn(mockResponse);

        // when & then
        this.mockMvc.perform(post("/api/v1/payments/create/{planId}", 1L)
                        .header("Authorization", "Bearer access_token_example")
                        .contentType("application/json")
                        .content(toJson(Map.of(
                                "auto_renew", false
                        ))))
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
                                        .pathParameters(
                                                parameterWithName("planId").description("구독 플랜 ID")
                                        )
                                        .requestFields(
                                                fieldWithPath("auto_renew").type(JsonFieldType.BOOLEAN).description("자동 갱신 여부 (true: 정기결제, false: 단편결제)")
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
    void approvePaymentTest() throws Exception {
        // given
        doNothing().when(paymentFacade).approvePayment(anyLong(), anyLong(), any(String.class), any(String.class));

        // when & then
        this.mockMvc.perform(post("/api/v1/payments/approve")
                        .header("Authorization", "Bearer access_token_example")
                        .contentType("application/json")
                        .content(toJson(Map.of(
                                "paymentId", 1L,
                                "orderId", "order_1_1234567890",
                                "pg_token", "05158049924b3495c98b"
                        ))))
                .andExpect(status().isOk())
                .andDo(document("payment-approve",
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

    @Test
    void getPaymentDetailTest() throws Exception {
        // given
        LocalDateTime now = LocalDateTime.now();
        PaymentDetailDto mockResponse = PaymentDetailDto.builder()
                .paymentId(1L)
                .userName("John Doe")
                .userEmail("john@example.com")
                .planName("Premium Plan")
                .amount(new BigDecimal("9900"))
                .status("COMPLETED")
                .createdAt(now)
                .paidAt(now)
                .build();
        given(paymentService.getPaymentDetail(anyLong(), anyLong())).willReturn(mockResponse);

        // when & then
        this.mockMvc.perform(get("/api/v1/payments/{paymentId}", 1L)
                        .header("Authorization", "Bearer access_token_example")
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andDo(document("payment-detail",
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("Payments")
                                        .summary("결제 상세 조회 (결제 ID)")
                                        .description("결제 ID로 결제 상세 정보를 조회합니다. 사용자 이름, 이메일, 구독 상품명이 포함됩니다.")
                                        .requestHeaders(
                                                headerWithName("Authorization").description("Bearer 토큰")
                                        )
                                        .pathParameters(
                                                parameterWithName("paymentId").description("결제 기록 ID")
                                        )
                                        .responseFields(
                                                fieldWithPath("status.statusCode").type(JsonFieldType.STRING).description("상태 코드"),
                                                fieldWithPath("status.message").type(JsonFieldType.STRING).description("상태 메시지"),
                                                fieldWithPath("status.description").type(JsonFieldType.STRING).description("상태 설명").optional(),
                                                fieldWithPath("body").type(JsonFieldType.OBJECT).description("응답 데이터"),
                                                fieldWithPath("body.paymentId").type(JsonFieldType.NUMBER).description("결제 기록 ID"),
                                                fieldWithPath("body.userName").type(JsonFieldType.STRING).description("사용자 이름"),
                                                fieldWithPath("body.userEmail").type(JsonFieldType.STRING).description("사용자 이메일"),
                                                fieldWithPath("body.planName").type(JsonFieldType.STRING).description("구독 상품명"),
                                                fieldWithPath("body.amount").type(JsonFieldType.NUMBER).description("결제 금액"),
                                                fieldWithPath("body.status").type(JsonFieldType.STRING).description("결제 상태"),
                                                fieldWithPath("body.createdAt").type(JsonFieldType.STRING).description("결제 생성 시간"),
                                                fieldWithPath("body.paidAt").type(JsonFieldType.STRING).description("결제 완료 시간"),
                                                fieldWithPath("body.subscriptionStartDate").type(JsonFieldType.STRING).description("구독 시작 시간").optional(),
                                                fieldWithPath("body.subscriptionEndDate").type(JsonFieldType.STRING).description("구독 종료 시간").optional()
                                        )
                                        .build()
                        )
                ));
    }

    @Test
    void getPaymentListTest() throws Exception {
        // given
        LocalDateTime now = LocalDateTime.now();
        List<PaymentDetailDto> mockList = List.of(
                PaymentDetailDto.builder()
                        .paymentId(1L)
                        .userName("John Doe")
                        .userEmail("john@example.com")
                        .planName("Premium Plan")
                        .amount(new BigDecimal("9900"))
                        .status("COMPLETED")
                        .createdAt(now)
                        .paidAt(now)
                        .build()
        );
        Page<PaymentDetailDto> mockPage = new PageImpl<>(mockList, PageRequest.of(0, 10), 1);
        given(paymentService.getPaymentList(anyLong(), any())).willReturn(mockPage);

        // when & then
        this.mockMvc.perform(get("/api/v1/payments/list?page=0&size=10")
                        .header("Authorization", "Bearer access_token_example")
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andDo(document("payment-list",
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("Payments")
                                        .summary("결제 내역 목록 조회")
                                        .description("사용자의 전체 결제 내역을 최신순으로 조회합니다. (페이지네이션 지원)")
                                        .requestHeaders(
                                                headerWithName("Authorization").description("Bearer 토큰")
                                        )
                                        .queryParameters(
                                                parameterWithName("page").description("페이지 번호 (0부터 시작)"),
                                                parameterWithName("size").description("페이지 크기")
                                        )
                                        .responseFields(
                                                fieldWithPath("status.statusCode").type(JsonFieldType.STRING).description("상태 코드"),
                                                fieldWithPath("status.message").type(JsonFieldType.STRING).description("상태 메시지"),
                                                fieldWithPath("status.description").type(JsonFieldType.STRING).description("상태 설명").optional(),
                                                fieldWithPath("body.totalElements").type(JsonFieldType.NUMBER).description("전체 결제 기록 수"),
                                                fieldWithPath("body.numberOfElements").type(JsonFieldType.NUMBER).description("반환된 결제 기록 수"),
                                                fieldWithPath("body.payments").type(JsonFieldType.ARRAY).description("결제 기록 배열"),
                                                fieldWithPath("body.payments[].paymentId").type(JsonFieldType.NUMBER).description("결제 기록 ID"),
                                                fieldWithPath("body.payments[].userName").type(JsonFieldType.STRING).description("사용자 이름"),
                                                fieldWithPath("body.payments[].userEmail").type(JsonFieldType.STRING).description("사용자 이메일"),
                                                fieldWithPath("body.payments[].planName").type(JsonFieldType.STRING).description("구독 상품명"),
                                                fieldWithPath("body.payments[].amount").type(JsonFieldType.NUMBER).description("결제 금액"),
                                                fieldWithPath("body.payments[].status").type(JsonFieldType.STRING).description("결제 상태"),
                                                fieldWithPath("body.payments[].createdAt").type(JsonFieldType.STRING).description("결제 생성 시간"),
                                                fieldWithPath("body.payments[].paidAt").type(JsonFieldType.STRING).description("결제 완료 시간"),
                                                fieldWithPath("body.payments[].subscriptionStartDate").type(JsonFieldType.STRING).description("구독 시작 시간").optional(),
                                                fieldWithPath("body.payments[].subscriptionEndDate").type(JsonFieldType.STRING).description("구독 종료 시간").optional()
                                        )
                                        .build()
                        )
                ));
    }

    @Test
    void getPaymentResultTest() throws Exception {
        // given
        PaymentResultDto mockResponse = PaymentResultDto.builder()
                .userName("John Doe")
                .userEmail("john@example.com")
                .planName("Premium Plan")
                .build();
        given(paymentService.getPaymentResult(anyLong())).willReturn(mockResponse);

        // when & then
        this.mockMvc.perform(get("/api/v1/payments/result")
                        .header("Authorization", "Bearer access_token_example")
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andDo(document("payment-result",
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("Payments")
                                        .summary("결제 완료/실패 조회")
                                        .description("사용자의 현재 활성 구독 정보를 조회합니다. 결제 완료/실패 페이지에서 필요한 정보입니다.")
                                        .requestHeaders(
                                                headerWithName("Authorization").description("Bearer 토큰")
                                        )
                                        .responseFields(
                                                fieldWithPath("status.statusCode").type(JsonFieldType.STRING).description("상태 코드"),
                                                fieldWithPath("status.message").type(JsonFieldType.STRING).description("상태 메시지"),
                                                fieldWithPath("status.description").type(JsonFieldType.STRING).description("상태 설명").optional(),
                                                fieldWithPath("body").type(JsonFieldType.OBJECT).description("응답 데이터"),
                                                fieldWithPath("body.userName").type(JsonFieldType.STRING).description("사용자 이름"),
                                                fieldWithPath("body.userEmail").type(JsonFieldType.STRING).description("사용자 이메일"),
                                                fieldWithPath("body.planName").type(JsonFieldType.STRING).description("구독 상품명")
                                        )
                                        .build()
                        )
                ));
    }
}
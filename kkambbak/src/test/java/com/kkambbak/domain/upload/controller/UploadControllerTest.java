package com.kkambbak.domain.upload.controller;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.kkambbak.KkambbakDocumentApiTester;
import com.kkambbak.domain.upload.service.FileStorageService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.request.RequestDocumentation;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.multipart;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UploadControllerTest extends KkambbakDocumentApiTester {

    @MockitoBean
    private FileStorageService fileStorageService;

    @Test
    void uploadImage() throws Exception {
        // given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        String mockR2Url = "https://pub-xxxxx.r2.dev/550e8400-e29b-41d4-a716-446655440000.webp";
        when(fileStorageService.uploadImage(any())).thenReturn(mockR2Url);

        // when & then
        this.mockMvc.perform(multipart("/api/v1/upload/image")
                        .file(file))
                .andExpect(status().isOk())
                .andDo(document("upload-image",
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("upload")
                                        .summary("이미지 파일 업로드")
                                        .description("이미지 파일을 업로드하고 Cloudflare R2 URL을 반환합니다. " +
                                                "업로드된 이미지는 자동으로 WebP 포맷으로 변환되며, 필요시 리사이징됩니다 (최대 1920x1920). " +
                                                "지원 포맷: JPG, PNG, GIF, BMP, WebP 등. " +
                                                "\n\n**요청 파라미터:**\n" +
                                                "- file (필수): 이미지 파일\n" +
                                                "  예시: test-image.jpg, photo.png\n" +
                                                "  형식: multipart/form-data")
                                        .responseFields(
                                                fieldWithPath("status.statusCode").type(JsonFieldType.STRING).description("상태 코드"),
                                                fieldWithPath("status.message").type(JsonFieldType.STRING).description("상태 메시지"),
                                                fieldWithPath("status.description").type(JsonFieldType.STRING).description("상태 설명").optional(),
                                                fieldWithPath("body.url").type(JsonFieldType.STRING).description("업로드된 이미지 URL (Cloudflare R2 호스팅, WebP 포맷)")
                                        )
                                        .build()
                        )
                ));
    }

    @Test
    void getImage() throws Exception {
        // given
        String imageId = "550e8400-e29b-41d4-a716-446655440000";
        byte[] imageBytes = "fake image binary data".getBytes();

        when(fileStorageService.downloadImage(anyString())).thenReturn(imageBytes);
        when(fileStorageService.getContentType(anyString())).thenReturn("image/webp");

        // when & then
        this.mockMvc.perform(get("/api/v1/upload/images/{imageId}", imageId))
                .andExpect(status().isOk())
                .andExpect(header().exists("Content-Type"))
                .andExpect(header().string("Content-Type", "image/webp"))
                .andExpect(header().string("Cache-Control", "public, max-age=604800"))
                .andExpect(header().string("Access-Control-Allow-Origin", "*"))
                .andDo(document("get-image",
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("upload")
                                        .summary("이미지 조회")
                                        .description("R2에 저장된 이미지를 바이너리로 반환합니다.\n\n" +
                                                "**요청 파라미터:**\n" +
                                                "- imageId (필수): 이미지 식별자 (UUID)\n" +
                                                "  예시: 550e8400-e29b-41d4-a716-446655440000\n\n" +
                                                "**응답 헤더:**\n" +
                                                "- Content-Type: 이미지 MIME 타입 (image/webp, image/jpeg 등)\n" +
                                                "- Cache-Control: 7일간 브라우저 캐싱\n" +
                                                "- Access-Control-Allow-Origin: CORS 허용\n\n" +
                                                "**응답 바디:**\n" +
                                                "- 이미지 바이너리 데이터")
                                        .pathParameters(
                                                RequestDocumentation.parameterWithName("imageId")
                                                        .description("이미지 식별자 (UUID 형식)")
                                        )
                                        .build()
                        )
                ));
    }
}
package com.jj.redline.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.jj.redline.common.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();

    @Test
    void redlineException은설정된상태코드로응답한다() {
        ResponseEntity<ApiResponse<Void>> response =
                globalExceptionHandler.handleRedlineException(new NotFoundException("없음"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).isEqualTo("없음");
    }

    @Test
    void 기타예외는500으로응답한다() {
        ResponseEntity<ApiResponse<Void>> response =
                globalExceptionHandler.handleException(new RuntimeException("boom"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
    }
}

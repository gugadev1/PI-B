package com.technova.web;

import com.technova.web.ApiDtos.ErroOutput;
import com.technova.exception.PersistenciaException;
import com.technova.exception.RecursoNaoEncontradoException;
import com.technova.exception.RegraNegocioException;
import com.technova.exception.ValidacaoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ApiExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(ValidacaoException.class)
    public ResponseEntity<ErroOutput> invalidInput(ValidacaoException exception) {
        return ResponseEntity.badRequest().body(new ErroOutput(exception.getMessage()));
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroOutput> notFound(RecursoNaoEncontradoException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErroOutput(exception.getMessage()));
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroOutput> businessRule(RegraNegocioException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErroOutput(exception.getMessage()));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErroOutput> httpError(ResponseStatusException exception) {
        return ResponseEntity.status(exception.getStatusCode())
                .body(new ErroOutput(exception.getReason() == null ? "Operação não permitida." : exception.getReason()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroOutput> conflict(DataIntegrityViolationException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErroOutput("Já existe um registro com esses dados ou há dependências vinculadas."));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroOutput> malformedRequest(HttpMessageNotReadableException exception) {
        return ResponseEntity.badRequest().body(new ErroOutput("O corpo da requisição é inválido."));
    }

    @ExceptionHandler(PersistenciaException.class)
    public ResponseEntity<ErroOutput> persistence(PersistenciaException exception) {
        LOGGER.error("Falha de persistência na API TechNova.", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErroOutput("Não foi possível concluir a operação no banco de dados."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroOutput> unexpected(Exception exception) {
        LOGGER.error("Erro inesperado na API TechNova.", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErroOutput("Não foi possível concluir a operação."));
    }
}

package com.ablil.minitrello.commons

import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(ConflictException::class)
    fun handleConflict(ex: ConflictException): ProblemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.message)

    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleResourceNotFound(ex: ConflictException): ProblemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.message)

    @ExceptionHandler(UnprocessableRequest::class)
    fun handleUnprocessableRequest(ex: UnprocessableRequest): ProblemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.message)

}

data class ConflictException(override val message: String) : RuntimeException(message)


data class ResourceNotFoundException(override val message: String) : RuntimeException(message)

class UnprocessableRequest(override val message: String) : RuntimeException(message)

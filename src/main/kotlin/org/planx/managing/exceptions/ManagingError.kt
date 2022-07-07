package org.planx.managing.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.planx.common.models.BaseException

class ManagingError(
    override val requestId: String = "",
    override var internalErrors: String? = ""
) : BaseException()

@ControllerAdvice
class EmployeeNotFoundAdvice {
    @ResponseBody
    @ExceptionHandler(ManagingError::class)
    @ResponseStatus(
        HttpStatus.BAD_REQUEST
    )
    fun employeeNotFoundHandler(ex: ManagingError): String? {
        return ex.internalErrors
    }
}

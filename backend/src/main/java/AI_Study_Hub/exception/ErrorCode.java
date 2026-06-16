package AI_Study_Hub.exception;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@NoArgsConstructor
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategoried error" , HttpStatus.INTERNAL_SERVER_ERROR),

    USERNAME_EXITED(2001, "User name have already existed!!" , HttpStatus.BAD_REQUEST),
    EMAIL_EXITED(2002, "Your email have already existed!!" , HttpStatus.BAD_REQUEST),
    NOT_FOUND(2003, "Not Found Role", HttpStatus.NOT_FOUND),
    USERNAME_NOT_EXITED(2004, "Your account is not exist!!!", HttpStatus.NOT_FOUND),
    PASSWORD_INCORRECTLY(2005, "Your password incorrect!!!", HttpStatus.NOT_FOUND),
    ACCOUNT_NOT_EXITS(2006, "Account is not exits", HttpStatus.NOT_FOUND),
    NEW_PASSWORD_INCORRECTLY(2007, "Your new password that you just confirm failed!!!" , HttpStatus.BAD_REQUEST),
    EMAIL_NOT_EXITS(2008, "Your email is not exits, please check your email again!!!", HttpStatus.NOT_FOUND),
    TOO_MANY_REQUEST(2009, "You have tried manay time , please try again after 1 minute", HttpStatus.BAD_REQUEST),
    INVALID_OTP(2010, "OTP not exit , please try again!!!",HttpStatus.BAD_REQUEST),
    PASSWORD_INVALIDATION(2011, "Your password must be at least 5 characterist!!!", HttpStatus.BAD_REQUEST),
    INVALID_DOB(2012, "You not enough age to sign yet!!!", HttpStatus.BAD_REQUEST),
    INVALID_USERNAME(2013, "Are you sure:), Change your name", HttpStatus.BAD_REQUEST),
    EMAIL_INVALID(2014, "Your email invalid", HttpStatus.BAD_REQUEST),

    UNAUTHENTICATED(1001, "Authenticated", HttpStatus.FORBIDDEN),
    UNAUTHORIZED(1002, "No permission", HttpStatus.FORBIDDEN),
    TOKEN_INVALID(1003, "Your token is invalid", HttpStatus.FORBIDDEN),
    ;

    int code;
    String message;
    HttpStatusCode statusCode;

    ErrorCode(int code , String message , HttpStatusCode statusCode){
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

}

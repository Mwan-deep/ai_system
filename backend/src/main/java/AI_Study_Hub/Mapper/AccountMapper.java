package AI_Study_Hub.Mapper;

import AI_Study_Hub.dto.request.AccountCreateRequest;
import AI_Study_Hub.dto.request.AccountUpdateRequest;
import AI_Study_Hub.dto.response.AccountResponse;
import AI_Study_Hub.entity.Account;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;


@Mapper(componentModel = "spring")
public interface AccountMapper {
    Account toAccount(AccountCreateRequest request);
    AccountResponse toAccountResponse(Account account);

    void toUpdateAccount(AccountUpdateRequest request , @MappingTarget Account account);

}

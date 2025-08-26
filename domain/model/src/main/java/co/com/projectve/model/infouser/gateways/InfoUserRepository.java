package co.com.projectve.model.infouser.gateways;

import co.com.projectve.model.infouser.InfoUser;
import reactor.core.publisher.Mono;

public interface InfoUserRepository {
    Mono<InfoUser>saveRequest(InfoUser infoUser);

}

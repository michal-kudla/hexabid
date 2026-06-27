package com.github.hexabid.statement.port.out;

import com.github.hexabid.statement.event.StatementDomainEvent;

public interface StatementEventPublisher {
    void publish(StatementDomainEvent event);
}

package com.joseph.petfound;

import org.springframework.data.repository.CrudRepository;

public interface MessageList extends CrudRepository<Message, Long> {
}

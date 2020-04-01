/**
 * Copyright (C) 2018-2020 Expedia, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.expediagroup.streamplatform.streamregistry.core.services;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Predicate;

import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.DataToModel;
import com.expediagroup.streamplatform.streamregistry.ModelToData;
import com.expediagroup.streamplatform.streamregistry.core.events.EventType;
import com.expediagroup.streamplatform.streamregistry.core.events.NotificationEventEmitter;
import com.expediagroup.streamplatform.streamregistry.core.handlers.HandlerService;
import com.expediagroup.streamplatform.streamregistry.core.repositories.StreamRepository;
import com.expediagroup.streamplatform.streamregistry.core.validators.StreamValidator;
import com.expediagroup.streamplatform.streamregistry.model.Stream;
import com.expediagroup.streamplatform.streamregistry.model.keys.StreamKey;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class StreamService {
  private final HandlerService handlerService;
  private final StreamValidator streamValidator;
  private final StreamRepository streamRepository;
  private final NotificationEventEmitter<Stream> streamServiceEventEmitter;

  public Optional<Stream> create(Stream stream) throws ValidationException {
    com.expediagroup.streamplatform.streamregistry.data.Stream data =
        ModelToData.convertStream(stream);

    if (streamRepository.findById(data.getKey()).isPresent()) {
      throw new ValidationException("Can't create because it already exists");
    }
    streamValidator.validateForCreate(stream);
    data.setSpecification(handlerService.handleInsert(ModelToData.convertStream(stream)));
    Stream out = DataToModel.convert(streamRepository.save(data));
    streamServiceEventEmitter.emitEventOnProcessedEntity(EventType.CREATE, out);
    return Optional.ofNullable(out);
  }

  public Optional<Stream> read(StreamKey key) {
    Optional<com.expediagroup.streamplatform.streamregistry.data.Stream> data =
        streamRepository.findById(ModelToData.convertStreamKey(key));
    return data.isPresent() ? Optional.of(DataToModel.convert(data.get())) : Optional.empty();
  }

  public Iterable<Stream> readAll() {
    ArrayList out = new ArrayList();
    for (com.expediagroup.streamplatform.streamregistry.data.Stream stream : streamRepository.findAll()) {
      out.add(DataToModel.convert(stream));
    }
    return out;
  }

  public Optional<Stream> update(Stream stream) throws ValidationException {
    com.expediagroup.streamplatform.streamregistry.data.Stream streamData =
        ModelToData.convertStream(stream);

    Optional<com.expediagroup.streamplatform.streamregistry.data.Stream> existing =
        streamRepository.findById(streamData.getKey());
    if (!existing.isPresent()) {
      throw new ValidationException("Can't update because it doesn't exist");
    }
    streamValidator.validateForUpdate(stream, DataToModel.convert(existing.get()));
    streamData.setSpecification(handlerService.handleInsert(streamData));
    Stream out = DataToModel.convert(streamRepository.save(streamData));
    streamServiceEventEmitter.emitEventOnProcessedEntity(EventType.UPDATE, out);
    return Optional.ofNullable(out);
  }

  public Optional<Stream> upsert(Stream stream) throws ValidationException {

    com.expediagroup.streamplatform.streamregistry.data.Stream streamData =
        ModelToData.convertStream(stream);

    return !streamRepository.findById(streamData.getKey()).isPresent() ?
        create(stream) :
        update(stream);
  }

  public void delete(Stream stream) {
    throw new UnsupportedOperationException();
  }

  public Iterable<Stream> findAll(Predicate<Stream> filter) {
    return streamRepository.findAll().stream().map(d -> DataToModel.convert(d)).filter(filter).collect(toList());
  }

  public boolean exists(StreamKey key) {
    return read(key).isEmpty();
  }

  @Deprecated
  public void validateStreamExists(StreamKey key) {
    if (!exists(key)) {
      throw new ValidationException("Stream does not exist");
    }
  }
}

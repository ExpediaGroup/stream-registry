package com.expediagroup.streamplatform.streamregistry.core.validators;

import com.expediagroup.streamplatform.streamregistry.core.validators.key.ConsumerKeyValidator;
import com.expediagroup.streamplatform.streamregistry.core.views.DomainView;
import com.expediagroup.streamplatform.streamregistry.core.views.StreamView;
import com.expediagroup.streamplatform.streamregistry.core.views.ZoneView;
import com.expediagroup.streamplatform.streamregistry.model.Consumer;
import com.expediagroup.streamplatform.streamregistry.model.Domain;
import com.expediagroup.streamplatform.streamregistry.model.Specification;
import com.expediagroup.streamplatform.streamregistry.model.Status;
import com.expediagroup.streamplatform.streamregistry.model.Stream;
import com.expediagroup.streamplatform.streamregistry.model.keys.ConsumerKey;
import com.expediagroup.streamplatform.streamregistry.repository.DomainRepository;
import com.expediagroup.streamplatform.streamregistry.repository.StreamRepository;
import com.expediagroup.streamplatform.streamregistry.repository.ZoneRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ConsumerValidatorTest {
  @Mock
  private StreamRepository streamRepository;

  @Mock
  private DomainRepository domainRepository;

  @Mock
  private ZoneRepository zoneRepository;

  private final ObjectMapper mapper = new ObjectMapper();

  private ConsumerValidator consumerValidator;

  @Before
  public void initialize() {
    consumerValidator = new ConsumerValidator(
      new StreamView(streamRepository),
      new ZoneView(zoneRepository),
      new DomainView(domainRepository),
      new ConsumerKeyValidator(),
      new SpecificationValidator()
    );
  }

  @Test
  public void consumerNameIsValid() {
    Consumer consumer = createTestConsumer();
    consumer.getKey().setName("1_name");
    ValidationException ex = assertThrows(ValidationException.class, () -> consumerValidator.validateForCreate(consumer));
    assertEquals(ex.getMessage(), "Invalid name '1_name'. Names must conform to pattern ^[a-z][a-z0-9]*(?:_[a-z0-9]+)*$");
  }

  @Test
  public void domainIsNotExist() {
    Consumer consumer = createTestConsumer();
    when(domainRepository.findById(any())).thenReturn(Optional.empty());
    ValidationException ex = assertThrows(ValidationException.class, () -> consumerValidator.validateForCreate(consumer));
    assertEquals(ex.getMessage(), "Domain does not exist");
  }

  @Test
  public void streamIsNotExist() {
    Consumer consumer = createTestConsumer();
    final Domain domainEntity = mock(Domain.class);
    when(domainRepository.findById(any())).thenReturn(Optional.of(domainEntity));
    when(streamRepository.findById(any())).thenReturn(Optional.empty());
    ValidationException ex = assertThrows(ValidationException.class, () -> consumerValidator.validateForCreate(consumer));
    assertEquals(ex.getMessage(), "Stream does not exist");
  }

  @Test
  public void ZoneIsNotExist() {
    Consumer consumer = createTestConsumer();
    final Domain domainEntity = mock(Domain.class);
    final Stream streamEntity = mock(Stream.class);
    when(domainRepository.findById(any())).thenReturn(Optional.of(domainEntity));
    when(streamRepository.findById(any())).thenReturn(Optional.of(streamEntity));
    when(zoneRepository.findById(any())).thenReturn(Optional.empty());
    ValidationException ex = assertThrows(ValidationException.class, () -> consumerValidator.validateForCreate(consumer));
    assertEquals(ex.getMessage(), "Zone does not exist");
  }

  public Consumer createTestConsumer() {
    Consumer consumer = new Consumer();
    consumer.setKey(new ConsumerKey("domain", "stream", 1, "zone", "consumer"));
    Specification specification = new Specification();
    specification.setType("egsp.kafka");
    specification.setConfiguration(mapper.createObjectNode());
    specification.setTags(Collections.emptyList());
    consumer.setSpecification(specification);
    Status status = new Status(mapper.createObjectNode());
    consumer.setStatus(status);
    return consumer;
  }
}

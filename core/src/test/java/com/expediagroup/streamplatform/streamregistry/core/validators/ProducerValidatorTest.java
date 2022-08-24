package com.expediagroup.streamplatform.streamregistry.core.validators;

import com.expediagroup.streamplatform.streamregistry.core.validators.key.ProcessKeyValidator;
import com.expediagroup.streamplatform.streamregistry.core.validators.key.ProducerKeyValidator;
import com.expediagroup.streamplatform.streamregistry.core.views.DomainView;
import com.expediagroup.streamplatform.streamregistry.core.views.StreamView;
import com.expediagroup.streamplatform.streamregistry.core.views.ZoneView;
import com.expediagroup.streamplatform.streamregistry.model.Producer;
import com.expediagroup.streamplatform.streamregistry.model.Domain;
import com.expediagroup.streamplatform.streamregistry.model.Specification;
import com.expediagroup.streamplatform.streamregistry.model.Status;
import com.expediagroup.streamplatform.streamregistry.model.Stream;
import com.expediagroup.streamplatform.streamregistry.model.keys.ProducerKey;
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
public class ProducerValidatorTest {

  @Mock
  private StreamRepository streamRepository;

  @Mock
  private DomainRepository domainRepository;

  @Mock
  private ZoneRepository zoneRepository;

  private final ObjectMapper mapper = new ObjectMapper();

  private ProducerValidator producerValidator;

  @Before
  public void initialize() {
    producerValidator = new ProducerValidator(
      new StreamView(streamRepository),
      new ZoneView(zoneRepository),
      new DomainView(domainRepository),
      new ProducerKeyValidator(),
      new SpecificationValidator()
    );
  }


  @Test
  public void producerNameIsValid() {
    Producer producer = createTestConsumer();
    producer.getKey().setName("1_name");
    ValidationException ex = assertThrows(ValidationException.class, () -> producerValidator.validateForCreate(producer));
    assertEquals(ex.getMessage(), "Invalid name '1_name'. Names must conform to pattern ^[a-z][a-z0-9]*(?:_[a-z0-9]+)*$");
  }

  @Test
  public void domainIsNotExist() {
    Producer producer = createTestConsumer();
    when(domainRepository.findById(any())).thenReturn(Optional.empty());
    ValidationException ex = assertThrows(ValidationException.class, () -> producerValidator.validateForCreate(producer));
    assertEquals(ex.getMessage(), "Domain does not exist");
  }

  @Test
  public void streamIsNotExist() {
    Producer producer = createTestConsumer();
    final Domain domainEntity = mock(Domain.class);
    when(domainRepository.findById(any())).thenReturn(Optional.of(domainEntity));
    when(streamRepository.findById(any())).thenReturn(Optional.empty());
    ValidationException ex = assertThrows(ValidationException.class, () -> producerValidator.validateForCreate(producer));
    assertEquals(ex.getMessage(), "Stream does not exist");
  }

  @Test
  public void ZoneIsNotExist() {
    Producer producer = createTestConsumer();
    final Domain domainEntity = mock(Domain.class);
    final Stream streamEntity = mock(Stream.class);
    when(domainRepository.findById(any())).thenReturn(Optional.of(domainEntity));
    when(streamRepository.findById(any())).thenReturn(Optional.of(streamEntity));
    when(zoneRepository.findById(any())).thenReturn(Optional.empty());
    ValidationException ex = assertThrows(ValidationException.class, () -> producerValidator.validateForCreate(producer));
    assertEquals(ex.getMessage(), "Zone does not exist");
  }

  public Producer createTestConsumer() {
    Producer producer = new Producer();
    producer.setKey(new ProducerKey("domain", "stream", 1, "zone", "producer"));
    Specification specification = new Specification();
    specification.setType("egsp.kafka");
    specification.setConfiguration(mapper.createObjectNode());
    specification.setTags(Collections.emptyList());
    producer.setSpecification(specification);
    Status status = new Status(mapper.createObjectNode());
    producer.setStatus(status);
    return producer;
  }
}

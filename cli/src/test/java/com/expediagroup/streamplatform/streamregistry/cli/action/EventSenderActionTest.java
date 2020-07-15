package com.expediagroup.streamplatform.streamregistry.cli.action;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.expediagroup.streamplatform.streamregistry.state.EventSender;
import com.expediagroup.streamplatform.streamregistry.state.model.event.Event;

@RunWith(MockitoJUnitRunner.class)
public class EventSenderActionTest {
  @Mock private EventSenderAction underTest;
  @Mock private EventSender sender;
  @Mock private CompletableFuture<Void> future;
  @Mock private Event<?, ?> event;

  @Before
  public void before() {
    when(underTest.sender()).thenReturn(sender);
    when(sender.send(any())).thenReturn(future);
    when(underTest.events()).thenReturn(List.of(event));
    doCallRealMethod().when(underTest).run(any(), any());
  }

  @Test
  public void success() {
    ByteArrayOutputStream baosOut = new ByteArrayOutputStream();
    PrintStream out = new PrintStream(baosOut);

    ByteArrayOutputStream baosErr = new ByteArrayOutputStream();
    PrintStream err = new PrintStream(baosErr);

    underTest.run(out, err);

    verify(sender).send(event);
    verify(future).join();

    assertThat(baosOut.toString(), is("Sent: event\n"));
    assertThat(baosErr.toString(), is(""));
  }

  @Test
  public void failure() {
    doThrow(CompletionException.class).when(future).join();

    ByteArrayOutputStream baosOut = new ByteArrayOutputStream();
    PrintStream out = new PrintStream(baosOut);

    ByteArrayOutputStream baosErr = new ByteArrayOutputStream();
    PrintStream err = new PrintStream(baosErr);

    underTest.run(out, err);

    assertThat(baosOut.toString(), is(""));
    assertThat(baosErr.toString(), is("java.util.concurrent.CompletionException\n"));
  }
}

package fr.rakambda.plexdeleter.amqp.message;

import java.io.Serializable;

public sealed interface IAmqpMessage extends Serializable permits TautulliMessage{
}

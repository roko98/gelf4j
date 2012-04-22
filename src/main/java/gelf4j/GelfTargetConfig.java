package gelf4j;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import org.json.simple.JSONValue;

/**
 * Configuration about how to create GELF messages in a particular logging framework.
 */
public class GelfTargetConfig
{
  public static final String FIELD_THREAD_NAME = "threadName";
  public static final String FIELD_TIMESTAMP_MS = "timestampMs";
  public static final String FIELD_LOGGER_NAME = "loggerName";
  public static final String FIELD_EXCEPTION = "exception";

  private String _host = "localhost";
  private int _port = GelfConnection.DEFAULT_PORT;

  private String _originHost;
  private String _facility;
  private final Map<String, Object> _additionalData = new HashMap<String, Object>();
  private final Map<String, String> _additionalFields;

  public GelfTargetConfig()
  {
    try
    {
      _originHost = InetAddress.getLocalHost().getHostName();
    }
    catch( final UnknownHostException uhe )
    {
      //ignore
    }
    _additionalFields = new HashMap<String, String>();
    _additionalFields.put( FIELD_EXCEPTION, FIELD_EXCEPTION );
  }

  public String getOriginHost()
  {
    return _originHost;
  }

  public void setOriginHost( final String originHost )
  {
    _originHost = originHost;
  }

  public String getFacility()
  {
    return _facility;
  }

  public void setFacility( final String facility )
  {
    _facility = facility;
  }

  public String getHost()
  {
    return _host;
  }

  public void setHost( final String host )
  {
    _host = host;
  }

  public int getPort()
  {
    return _port;
  }

  public void setPort( final int port )
  {
    _port = port;
  }

  public GelfConnection createConnection()
    throws Exception
  {
    try
    {
      return new GelfConnection( InetAddress.getByName( getHost() ), getPort() );
    }
    catch( final UnknownHostException uhe )
    {
      throw new Exception( "Unknown GELF host " + getHost(), uhe );
    }
    catch( final Exception e )
    {
      throw new Exception( "Error connecting to GELF host " + getHost() + " on port " + getPort(), e );
    }
  }

  /**
   * Additional fields to add to the gelf message. The keys are the key in the GELF message and the value corresponds
   * to a symbol recognized by the underlying log system.
   * 
   * <p>There are some common fields recognized by multiple frameworks (See FIELD_* constants) but in most cases
   * this will result in access to application specific data such as Mapped Diagnostic Contexts (MDC) in Log4j and
   * Logback.</p>
   */
  public Map<String, String> getAdditionalFields()
  {
    return _additionalFields;
  }

  public void setAdditionalFields( final String additionalFields )
  {
    _additionalFields.clear();
    for( Map.Entry<String, Object> entry : parseJsonObject( additionalFields ).entrySet() )
    {
      _additionalFields.put( entry.getKey(), String.valueOf( entry.getValue() ) );
    }
  }

  /**
   * Add an additional field. The format is mostly of use for logback configurator.
   *
   * @param fieldSpec This must be in format key=value where key is the GELF field, and value is symbolic key. e.g "ip_address=ipAddress"
   */
  public void addAdditionalField( final String fieldSpec )
  {
    final String[] field = parseField( fieldSpec );
    getAdditionalFields().put( field[0], field[1] );
  }

  /**
   * @return the set of data fields that will be added to the GELF message.
   */
  public Map<String, Object> getAdditionalData()
  {
    return _additionalData;
  }

  public void setAdditionalData( final String additionalData )
  {
    _additionalData.clear();
    _additionalData.putAll( parseJsonObject( additionalData ) );
  }

  public void addAdditionalData( final String fieldSpec )
  {
    final String[] field = parseField( fieldSpec );
    getAdditionalData().put( field[0], field[1] );
  }

  @SuppressWarnings( "unchecked" )
  private Map<String, Object> parseJsonObject( final String additionalFields )
  {
    return (Map<String, Object>) JSONValue.parse( additionalFields.replaceAll( "'", "\"" ) );
  }

  private String[] parseField( final String fieldSpec )
  {
    final int index = fieldSpec.indexOf( "=" );
    if( -1 == index )
    {
      throw new IllegalArgumentException( "Expected value to be of form a=b but found '" + fieldSpec + "' instead." );
    }
    final String key = fieldSpec.substring( 0, index );
    final String value = fieldSpec.substring( index + 1 );
    return new String[]{ key, value };
  }
}

package client;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the client package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _HelloResponse_QNAME = new QName("http://helloworld/", "HelloResponse");
    private final static QName _Hello_QNAME = new QName("http://helloworld/", "Hello");
    private final static QName _HelloFooResponse_QNAME = new QName("http://helloworld/", "HelloFooResponse");
    private final static QName _HelloFoo_QNAME = new QName("http://helloworld/", "HelloFoo");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: client
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link HelloFooResponse }
     * 
     */
    public HelloFooResponse createHelloFooResponse() {
        return new HelloFooResponse();
    }

    /**
     * Create an instance of {@link HelloFoo }
     * 
     */
    public HelloFoo createHelloFoo() {
        return new HelloFoo();
    }

    /**
     * Create an instance of {@link HelloResponse }
     * 
     */
    public HelloResponse createHelloResponse() {
        return new HelloResponse();
    }

    /**
     * Create an instance of {@link Hello }
     * 
     */
    public Hello createHello() {
        return new Hello();
    }

    /**
     * Create an instance of {@link Foo }
     * 
     */
    public Foo createFoo() {
        return new Foo();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link HelloResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://helloworld/", name = "HelloResponse")
    public JAXBElement<HelloResponse> createHelloResponse(HelloResponse value) {
        return new JAXBElement<HelloResponse>(_HelloResponse_QNAME, HelloResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Hello }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://helloworld/", name = "Hello")
    public JAXBElement<Hello> createHello(Hello value) {
        return new JAXBElement<Hello>(_Hello_QNAME, Hello.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link HelloFooResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://helloworld/", name = "HelloFooResponse")
    public JAXBElement<HelloFooResponse> createHelloFooResponse(HelloFooResponse value) {
        return new JAXBElement<HelloFooResponse>(_HelloFooResponse_QNAME, HelloFooResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link HelloFoo }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://helloworld/", name = "HelloFoo")
    public JAXBElement<HelloFoo> createHelloFoo(HelloFoo value) {
        return new JAXBElement<HelloFoo>(_HelloFoo_QNAME, HelloFoo.class, null, value);
    }

}

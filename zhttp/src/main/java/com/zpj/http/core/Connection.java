package com.zpj.http.core;

import com.zpj.http.ZHttp;
import com.zpj.http.exception.HttpStatusException;
import com.zpj.http.exception.UncheckedIOException;
import com.zpj.http.exception.UnsupportedMimeTypeException;
import com.zpj.http.parser.html.Parser;
import com.zpj.http.parser.html.nodes.Document;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.Proxy;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLSocketFactory;

/**
 * A Connection provides a convenient interface to fetch content from the web, and parse them into Documents.
 * <p>
 * To get a new Connection, use {@link ZHttp#connect(String)}. Connections contain {@link Request}
 * and {@link Response} objects. The request objects are reusable as prototype requests.
 * </p>
 * <p>
 * Request configuration can be made using either the shortcut methods in Connection (e.g. {@link #userAgent(String)}),
 * or by methods in the Connection.Request object directly. All request configuration must be made before the request is
 * executed.
 * </p>
 */
public interface Connection {

    /**
     * GET and POST http methods.
     */
    enum Method {
        GET(false), POST(true), PUT(true), DELETE(false), PATCH(true), HEAD(false), OPTIONS(false), TRACE(false);

        private final boolean hasBody;

        Method(boolean hasBody) {
            this.hasBody = hasBody;
        }

        /**
         * Check if this HTTP method has/needs a request body
         * @return if body needed
         */
        public final boolean hasBody() {
            return hasBody;
        }
    }

    /**
     * Set the request URL to fetch. The protocol must be HTTP or HTTPS.
     * @param url URL to connect to
     * @return this Connection, for chaining
     */
    Connection url(URL url);

    /**
     * Set the request URL to fetch. The protocol must be HTTP or HTTPS.
     * @param url URL to connect to
     * @return this Connection, for chaining
     */
    Connection url(String url);

    /**
     * Set the proxy to use for this request. Set to <code>null</code> to disable.
     * @param proxy proxy to use
     * @return this Connection, for chaining
     */
    Connection proxy(Proxy proxy);

    /**
     * Set the HTTP proxy to use for this request.
     * @param host the proxy hostname
     * @param port the proxy port
     * @return this Connection, for chaining
     */
    Connection proxy(String host, int port);

    /**
     * Set the request user-agent header.
     * @param userAgent user-agent to use
     * @return this Connection, for chaining
     */
    Connection userAgent(String userAgent);

    /**
     * Set the total request timeout duration. If a timeout occurs, an {@link java.net.SocketTimeoutException} will be thrown.
     * <p>The default timeout is <b>30 seconds</b> (30,000 millis). A timeout of zero is treated as an infinite timeout.
     * <p>Note that this timeout specifies the combined maximum duration of the connection time and the time to read
     * the full response.
     * @param millis number of milliseconds (thousandths of a second) before timing out connects or reads.
     * @return this Connection, for chaining
     * @see #maxBodySize(int)
     */
    Connection timeout(int millis);

    /**
     * Set the maximum bytes to read from the (uncompressed) connection into the body, before the connection is closed,
     * and the input truncated. The default maximum is 1MB. A max size of zero is treated as an infinite amount (bounded
     * only by your patience and the memory available on your machine).
     * @param bytes number of bytes to read from the input before truncating
     * @return this Connection, for chaining
     */
    Connection maxBodySize(int bytes);

    /**
     * Set the request referrer (aka "referer") header.
     * @param referrer referrer to use
     * @return this Connection, for chaining
     */
    Connection referrer(String referrer);

    Connection contentType(String contentType);

//    /**
//     * Configures the connection to (not) follow server redirects. By default this is <b>true</b>.
//     * @param followRedirects true if server redirects should be followed.
//     * @return this Connection, for chaining
//     */
//    Connection followRedirects(boolean followRedirects);

    /**
     * Set the request method to use, GET or POST. Default is GET.
     * @param method HTTP request method
     * @return this Connection, for chaining
     */
    Connection method(Method method);

    /**
     * Configures the connection to not throw exceptions when a HTTP error occurs. (4xx - 5xx, e.g. 404 or 500). By
     * default this is <b>false</b>; an IOException is thrown if an error is encountered. If set to <b>true</b>, the
     * response is populated with the error body, and the status message will reflect the error.
     * @param ignoreHttpErrors - false (default) if HTTP errors should be ignored.
     * @return this Connection, for chaining
     */
    Connection ignoreHttpErrors(boolean ignoreHttpErrors);

    /**
     * Ignore the document's Content-Type when parsing the response. By default this is <b>false</b>, an unrecognised
     * content-type will cause an IOException to be thrown. (This is to prevent producing garbage by attempting to parse
     * a JPEG binary image, for example.) Set to true to force a parse attempt regardless of content type.
     * @param ignoreContentType set to true if you would like the content type ignored on parsing the response into a
     * Document.
     * @return this Connection, for chaining
     */
    Connection ignoreContentType(boolean ignoreContentType);

    /**
     * Disable/enable TLS certificates validation for HTTPS requests.
     * <p>
     * By default this is <b>true</b>; all
     * connections over HTTPS perform normal validation of certificates, and will abort requests if the provided
     * certificate does not validate.
     * </p>
     * <p>
     * Some servers use expired, self-generated certificates; or your JDK may not
     * support SNI hosts. In which case, you may want to enable this setting.
     * </p>
     * <p>
     * <b>Be careful</b> and understand why you need to disable these validations.
     * </p>
     * @param value if should validate TLS (SSL) certificates. <b>true</b> by default.
     * @return this Connection, for chaining
     * @deprecated as distributions (specifically Google Play) are starting to show warnings if these checks are
     * disabled.
     */
    Connection validateTLSCertificates(boolean value);

    /**
     * Set custom SSL socket factory
     * @param sslSocketFactory custom SSL socket factory
     * @return this Connection, for chaining
     */
    Connection sslSocketFactory(SSLSocketFactory sslSocketFactory);

    /**
     * Add a request data parameter. Request parameters are sent in the request query string for GETs, and in the
     * request body for POSTs. A request may have multiple values of the same name.
     * @param key data key
     * @param value data value
     * @return this Connection, for chaining
     */
    Connection data(String key, String value);

    /**
     * Add an input stream as a request data parameter. For GETs, has no effect, but for POSTS this will upload the
     * input stream.
     * @param key data key (form item name)
     * @param filename the name of the file to present to the remove server. Typically just the name, not path,
     * component.
     * @param inputStream the input stream to upload, that you probably obtained from a {@link java.io.FileInputStream}.
     * You must close the InputStream in a {@code finally} block.
     * @return this Connections, for chaining
     * @see #data(String, String, InputStream, String) if you want to set the uploaded file's mimetype.
     */
    Connection data(String key, String filename, InputStream inputStream);

    Connection data(String key, String filename, InputStream inputStream, IHttp.OnStreamWriteListener listener);

    /**
     * Add an input stream as a request data parameter. For GETs, has no effect, but for POSTS this will upload the
     * input stream.
     * @param key data key (form item name)
     * @param filename the name of the file to present to the remove server. Typically just the name, not path,
     * component.
     * @param inputStream the input stream to upload, that you probably obtained from a {@link java.io.FileInputStream}.
     * @param contentType the Content Type (aka mimetype) to specify for this file.
     * You must close the InputStream in a {@code finally} block.
     * @return this Connections, for chaining
     */
    Connection data(String key, String filename, InputStream inputStream, String contentType);

    /**
     * Adds all of the supplied data to the request data parameters
     * @param data collection of data parameters
     * @return this Connection, for chaining
     */
    Connection data(Collection<KeyVal> data);

    /**
     * Adds all of the supplied data to the request data parameters
     * @param data map of data parameters
     * @return this Connection, for chaining
     */
    Connection data(Map<String, String> data);

    /**
     * Add a number of request data parameters. Multiple parameters may be set at once, e.g.: <code>.data("name",
     * "jsoup", "language", "Java", "language", "English");</code> creates a query string like:
     * <code>{@literal ?name=jsoup&language=Java&language=English}</code>
     * @param keyvals a set of key value pairs.
     * @return this Connection, for chaining
     */
    Connection data(String... keyvals);

    /**
     * Get the data KeyVal for this key, if any
     * @param key the data key
     * @return null if not set
     */
    KeyVal data(String key);

    /**
     * Set a POST (or PUT) request body. Useful when a server expects a plain request body, not a set for URL
     * encoded form key/value pairs. E.g.:
     * <code><pre>ZHttp.connect(url)
     * .requestBody(json)
     * .header("Content-Type", "application/json")
     * .post();</pre></code>
     * If any data key/vals are supplied, they will be sent as URL query params.
     * @return this Request, for chaining
     */
    Connection requestBody(String body);

    /**
     * Set a request header.
     * @param name header name
     * @param value header value
     * @return this Connection, for chaining
     * @see Request#headers()
     */
    Connection header(String name, String value);

    /**
     * Adds each of the supplied headers to the request.
     * @param headers map of headers name {@literal ->} value pairs
     * @return this Connection, for chaining
     * @see Request#headers()
     */
    Connection headers(Map<String, String> headers);

    /**
     * Set a cookie to be sent in the request.
     * @param cookie str of cookies
     * @return this Connection, for chaining
     */
    Connection cookie(String cookie);

    /**
     * Set a cookie to be sent in the request.
     * @param name name of cookie
     * @param value value of cookie
     * @return this Connection, for chaining
     */
    Connection cookie(String name, String value);

    /**
     * Adds each of the supplied cookies to the request.
     * @param cookies map of cookie name {@literal ->} value pairs
     * @return this Connection, for chaining
     */
    Connection cookies(Map<String, String> cookies);

    /**
     * Provide an alternate parser to use when parsing the response to a Document. If not set, defaults to the HTML
     * parser, unless the response content-type is XML, in which case the XML parser is used.
     * @param parser alternate parser
     * @return this Connection, for chaining
     */
    Connection parser(Parser parser);

    /**
     * Sets the default post data character set for x-www-form-urlencoded post data
     * @param charset character set to encode post data
     * @return this Connection, for chaining
     */
    Connection postDataCharset(String charset);

//    /**
//     * Execute the request as a GET, and parse the result.
//     * @return parsed Document
//     * @throws java.net.MalformedURLException if the request URL is not a HTTP or HTTPS URL, or is otherwise malformed
//     * @throws HttpStatusException if the response is not OK and HTTP response errors are not ignored
//     * @throws UnsupportedMimeTypeException if the response mime type is not supported and those errors are not ignored
//     * @throws java.net.SocketTimeoutException if the connection times out
//     * @throws IOException on error
//     */
//    Document get() throws IOException;
//
//    /**
//     * Execute the request as a POST, and parse the result.
//     * @return parsed Document
//     * @throws java.net.MalformedURLException if the request URL is not a HTTP or HTTPS URL, or is otherwise malformed
//     * @throws HttpStatusException if the response is not OK and HTTP response errors are not ignored
//     * @throws UnsupportedMimeTypeException if the response mime type is not supported and those errors are not ignored
//     * @throws java.net.SocketTimeoutException if the connection times out
//     * @throws IOException on error
//     */
//    Document post() throws IOException;

//    OutputStream outputStream() throws IOException;

    String toStr() throws IOException;

    Document toHtml() throws IOException;

    JSONObject toJsonObject() throws IOException, JSONException;

    JSONArray toJsonArray() throws IOException, JSONException;

    Document toXml() throws IOException;

    Connection onRedirect(IHttp.OnRedirectListener listener);

    Connection onError(IHttp.OnErrorListener listener);

    Connection onSuccess(IHttp.OnSuccessListener listener);

    /**
     * Execute the request.
     * @return a response object
     * @throws java.net.MalformedURLException if the request URL is not a HTTP or HTTPS URL, or is otherwise malformed
     * @throws HttpStatusException if the response is not OK and HTTP response errors are not ignored
     * @throws UnsupportedMimeTypeException if the response mime type is not supported and those errors are not ignored
     * @throws java.net.SocketTimeoutException if the connection times out
     * @throws IOException on error
     */
    Response execute() throws IOException;

    /**
     * Get the request object associated with this connection
     * @return request
     */
    Request request();

//    /**
//     * Set the connection's request
//     * @param request new request object
//     * @return this Connection, for chaining
//     */
//    Connection request(Request request);

    /**
     * Get the response, once the request has been executed
     * @return response
     */
    Response response();

//    /**
//     * Set the connection's response
//     * @param response new response
//     * @return this Connection, for chaining
//     */
//    Connection response(Response response);

    /**
     * Common methods for Requests and Responses
     * @param <T> Type of Base, either Request or Response
     */
    interface Base<T extends Base> {

        /**
         * Get the URL
         * @return URL
         */
        URL url();

        /**
         * Set the URL
         * @param url new URL
         * @return this, for chaining
         */
        T url(URL url);

        /**
         * Get the request method
         * @return method
         */
        Method method();

        /**
         * Set the request method
         * @param method new method
         * @return this, for chaining
         */
        T method(Method method);

        /**
         * Get the value of a header. If there is more than one header value with the same name, the headers are returned
         * comma seperated, per <a href="https://www.w3.org/Protocols/rfc2616/rfc2616-sec4.html#sec4.2">rfc2616-sec4</a>.
         * <p>
         * Header names are case insensitive.
         * </p>
         * @param name name of header (case insensitive)
         * @return value of header, or null if not set.
         * @see #hasHeader(String)
         * @see #cookie(String)
         */
        String header(String name);

        /**
         * Get the values of a header.
         * @param name header name, case insensitive.
         * @return a list of values for this header, or an empty list if not set.
         */
        List<String> headers(String name);

        /**
         * Set a header. This method will overwrite any existing header with the same case insensitive name. (If there
         * is more than one value for this header, this method will update the first matching header.
         * @param name Name of header
         * @param value Value of header
         * @return this, for chaining
         * @see #addHeader(String, String)
         */
        T header(String name, String value);

        /**
         * Add a header. The header will be added regardless of whether a header with the same name already exists.
         * @param name Name of new header
         * @param value Value of new header
         * @return this, for chaining
         */
        T addHeader(String name, String value);

        /**
         * Check if a header is present
         * @param name name of header (case insensitive)
         * @return if the header is present in this request/response
         */
        boolean hasHeader(String name);

        /**
         * Check if a header is present, with the given value
         * @param name header name (case insensitive)
         * @param value value (case insensitive)
         * @return if the header and value pair are set in this req/res
         */
        boolean hasHeaderWithValue(String name, String value);

        /**
         * Remove headers by name. If there is more than one header with this name, they will all be removed.
         * @param name name of header to remove (case insensitive)
         * @return this, for chaining
         */
        T removeHeader(String name);

        /**
         * Retrieve all of the request/response header names and corresponding values as a map. For headers with multiple
         * values, only the first header is returned.
         * <p>Note that this is a view of the headers only, and changes made to this map will not be reflected in the
         * request/response object.</p>
         * @return headers
         * @see #multiHeaders()

         */
        Map<String, String> headers();

        /**
         * Retreive all of the headers, keyed by the header name, and with a list of values per header.
         * @return a list of multiple values per header.
         */
        Map<String, List<String>> multiHeaders();

        /**
         * Get a cookie value by name from this request/response.
         * <p>
         * Response objects have a simplified cookie model. Each cookie set in the response is added to the response
         * object's cookie key=value map. The cookie's path, domain, and expiry date are ignored.
         * </p>
         * @param name name of cookie to retrieve.
         * @return value of cookie, or null if not set
         */
        String cookie(String name);

        /**
         * Set a cookie in this request/response.
         * @param name name of cookie
         * @param value value of cookie
         * @return this, for chaining
         */
        T cookie(String name, String value);

        /**
         * Check if a cookie is present
         * @param name name of cookie
         * @return if the cookie is present in this request/response
         */
        boolean hasCookie(String name);

        /**
         * Remove a cookie by name
         * @param name name of cookie to remove
         * @return this, for chaining
         */
        T removeCookie(String name);

        /**
         * Retrieve all of the request/response cookies as a map
         * @return cookies
         */
        Map<String, String> cookies();
    }

    /**
     * Represents a HTTP request.
     */
    interface Request extends Base<Request> {
        /**
         * Get the proxy used for this request.
         * @return the proxy; <code>null</code> if not enabled.
         */
        Proxy proxy();

        /**
         * Update the proxy for this request.
         * @param proxy the proxy ot use; <code>null</code> to disable.
         * @return this Request, for chaining
         */
        Request proxy(Proxy proxy);

        /**
         * Set the HTTP proxy to use for this request.
         * @param host the proxy hostname
         * @param port the proxy port
         * @return this Connection, for chaining
         */
        Request proxy(String host, int port);

        /**
         * Get the request timeout, in milliseconds.
         * @return the timeout in milliseconds.
         */
        int timeout();

        /**
         * Update the request timeout.
         * @param millis timeout, in milliseconds
         * @return this Request, for chaining
         */
        Request timeout(int millis);

        /**
         * Get the maximum body size, in bytes.
         * @return the maximum body size, in bytes.
         */
        int maxBodySize();

        /**
         * Update the maximum body size, in bytes.
         * @param bytes maximum body size, in bytes.
         * @return this Request, for chaining
         */
        Request maxBodySize(int bytes);

//        /**
//         * Get the current followRedirects configuration.
//         * @return true if followRedirects is enabled.
//         */
//        boolean followRedirects();
//
//        /**
//         * Configures the request to (not) follow server redirects. By default this is <b>true</b>.
//         * @param followRedirects true if server redirects should be followed.
//         * @return this Request, for chaining
//         */
//        Request followRedirects(boolean followRedirects);

        /**
         * Get the current ignoreHttpErrors configuration.
         * @return true if errors will be ignored; false (default) if HTTP errors will cause an IOException to be
         * thrown.
         */
        boolean ignoreHttpErrors();

        /**
         * Configures the request to ignore HTTP errors in the response.
         * @param ignoreHttpErrors set to true to ignore HTTP errors.
         * @return this Request, for chaining
         */
        Request ignoreHttpErrors(boolean ignoreHttpErrors);

        /**
         * Get the current ignoreContentType configuration.
         * @return true if invalid content-types will be ignored; false (default) if they will cause an IOException to
         * be thrown.
         */
        boolean ignoreContentType();

        /**
         * Configures the request to ignore the Content-Type of the response.
         * @param ignoreContentType set to true to ignore the content type.
         * @return this Request, for chaining
         */
        Request ignoreContentType(boolean ignoreContentType);

        /**
         * Get the current state of TLS (SSL) certificate validation.
         * @return true if TLS cert validation enabled
         * @deprecated
         */
        boolean validateTLSCertificates();

        /**
         * Set TLS certificate validation. <b>True</b> by default.
         * @param value set false to ignore TLS (SSL) certificates
         * @deprecated as distributions (specifically Google Play) are starting to show warnings if these checks are
         * disabled. This method will be removed in the next release.
         * @see #sslSocketFactory(SSLSocketFactory)
         */
        void validateTLSCertificates(boolean value);

        /**
         * Get the current custom SSL socket factory, if any.
         * @return custom SSL socket factory if set, null otherwise
         */
        SSLSocketFactory sslSocketFactory();

        /**
         * Set a custom SSL socket factory.
         * @param sslSocketFactory SSL socket factory
         */
        void sslSocketFactory(SSLSocketFactory sslSocketFactory);

        /**
         * Add a data parameter to the request
         * @param keyval data to add.
         * @return this Request, for chaining
         */
        Request data(KeyVal keyval);

        /**
         * Get all of the request's data parameters
         * @return collection of keyvals
         */
        Collection<KeyVal> data();

        /**
         * Set a POST (or PUT) request body. Useful when a server expects a plain request body, not a set for URL
         * encoded form key/value pairs. E.g.:
         * <code><pre>ZHttp.connect(url)
         * .requestBody(json)
         * .header("Content-Type", "application/json")
         * .post();</pre></code>
         * If any data key/vals are supplied, they will be sent as URL query params.
         * @return this Request, for chaining
         */
        Request requestBody(String body);

        /**
         * Get the current request body.
         * @return null if not set.
         */
        String requestBody();

        /**
         * Specify the parser to use when parsing the document.
         * @param parser parser to use.
         * @return this Request, for chaining
         */
        Request parser(Parser parser);

        /**
         * Get the current parser to use when parsing the document.
         * @return current Parser
         */
        Parser parser();

        /**
         * Sets the post data character set for x-www-form-urlencoded post data
         * @param charset character set to encode post data
         * @return this Request, for chaining
         */
        Request postDataCharset(String charset);

        /**
         * Gets the post data character set for x-www-form-urlencoded post data
         * @return character set to encode post data
         */
        String postDataCharset();

        Request onRedirect(IHttp.OnRedirectListener listener);

        IHttp.OnRedirectListener getOnRedirectListener();
    }

    /**
     * Represents a HTTP response.
     */
    interface Response extends Base<Response> {

        /**
         * Get the status code of the response.
         * @return status code
         */
        int statusCode();

        /**
         * Get the status message of the response.
         * @return status message
         */
        String statusMessage();

        /**
         * Get the character set name of the response, derived from the content-type header.
         * @return character set name
         */
        String charset();

        /**
         * Set / override the response character set. When the document body is parsed it will be with this charset.
         * @param charset to decode body as
         * @return this Response, for chaining
         */
        Response charset(String charset);

        /**
         * Get the response content type (e.g. "text/html");
         * @return the response content type
         */
        String contentType();

        /**
         * Read and parse the body of the response as a Document. If you intend to parse the same response multiple
         * times, you should {@link #bufferUp()} first.
         * @return a parsed Document
         * @throws IOException on error
         */
        Document parse() throws IOException;

        /**
         * Get the body of the response as a plain string.
         * @return body
         */
        String body();

        /**
         * Get the body of the response as an array of bytes.
         * @return body bytes
         */
        byte[] bodyAsBytes();

        /**
         * Read the body of the response into a local buffer, so that {@link #parse()} may be called repeatedly on the
         * same connection response (otherwise, once the response is read, its InputStream will have been drained and
         * may not be re-read). Calling {@link #body() } or {@link #bodyAsBytes()} has the same effect.
         * @return this response, for chaining
         * @throws UncheckedIOException if an IO exception occurs during buffering.
         */
        Response bufferUp();

        /**
         * Get the body of the response as a (buffered) InputStream. You should close the input stream when you're done with it.
         * Other body methods (like bufferUp, body, parse, etc) will not work in conjunction with this method.
         * <p>This method is useful for writing large responses to disk, without buffering them completely into memory first.</p>
         * @return the response body input stream
         */
        BufferedInputStream bodyStream();
    }

    /**
     * A Key:Value tuple(+), used for form data.
     */
    interface KeyVal {

        /**
         * Update the key of a keyval
         * @param key new key
         * @return this KeyVal, for chaining
         */
        KeyVal key(String key);

        /**
         * Get the key of a keyval
         * @return the key
         */
        String key();

        /**
         * Update the value of a keyval
         * @param value the new value
         * @return this KeyVal, for chaining
         */
        KeyVal value(String value);

        /**
         * Get the value of a keyval
         * @return the value
         */
        String value();

        /**
         * Add or update an input stream to this keyVal
         * @param inputStream new input stream
         * @return this KeyVal, for chaining
         */
        KeyVal inputStream(InputStream inputStream);

        /**
         * Get the input stream associated with this keyval, if any
         * @return input stream if set, or null
         */
        InputStream inputStream();

        /**
         * Does this keyval have an input stream?
         * @return true if this keyval does indeed have an input stream
         */
        boolean hasInputStream();

        /**
         * Set the Content Type header used in the MIME body (aka mimetype) when uploading files.
         * Only useful if {@link #inputStream(InputStream)} is set.
         * <p>Will default to {@code application/octet-stream}.</p>
         * @param contentType the new content type
         * @return this KeyVal
         */
        KeyVal contentType(String contentType);

        /**
         * Get the current Content Type, or {@code null} if not set.
         * @return the current Content Type.
         */
        String contentType();

        KeyVal setListener(IHttp.OnStreamWriteListener listener);

        IHttp.OnStreamWriteListener getListener();

    }
}

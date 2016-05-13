# vedaa-web

vedaa-web is a minimal web framework for Scala supporting both servlets and portlets.

This documention covers the servlet part of the framework. For portlets, see "vedaa-web-portlet".

## Getting started

Clone the project from GitHub and publish it to your local repository by running the [SBT](http://www.scala-sbt.org/) command `publish-local` on it.

To use the framework in your SBT-based project, add the following dependency to your `.sbt` build file:

```scala
libraryDependencies += "no.vedaadata" %% "vedaa-web" % "1.5-SNAPSHOT"
```
You also need the Java Servlet API:

```scala
libraryDependencies += "javax.servlet" % "servlet-api" % "2.5" % "provided"
```
To package a web application with SBT you also need the [xsbt-web-plugin](https://github.com/JamesEarlDouglas/xsbt-web-plugin).

See the [example applications](https://github.com/kavedaa/vedaa-web-examples/tree/master/examples-applications) for a complete build file setup.

## Usage

Traditionally using Java servlets you'd typically have one servlet for each page or action in your web application. With a framework like vedaa-web, instead you'll typically have a single servlet that receives all requests.

Your `web.xml` file may thus look like this:

```xml
    <servlet>
        <servlet-name>myServlet</servlet-name>
        <servlet-class>mypackage.MyServlet</servlet-class>
    </servlet>
    <servlet-mapping>
        <servlet-name>myServlet</servlet-name>
        <url-pattern>/</url-pattern>
    </servlet-mapping>
```
`MyServlet` will here be an instance of `no.vedaadata.web.servlet.RouterServlet`. 

You can of course also here use several servlets, or mix and match with traditional servlets or even other similar frameworks, in the same web application. 

The simplest working servlet implementation looks like this:

```scala
class MyServlet extends RouterServlet {

  route {

    case _ => "Hello world"
  }
}
```

### Routing

vedaa-web uses *pattern matching* and *extractors* for processing the request. As you have probably guessed, the example above matches all requests. Let's look at a couple of examples where we take apart the URL and return different responses depending on its content:

```scala
route {

  case get("index.html" /: _) => "Matches a GET request of 'index.html' followed by anything"

  case post("index.html" /: Nil) => "Matches a POST request of exactly 'index.html'"
}
```

You can also extract the values of any of the path elements:

```scala
  case get("foo" /: bar /: "zip" /: _) => s"The value of bar is $bar"

  case get("article" /: int(id) /: articleName /: Nil) => s"You requested the article with id $id and name $articleName"
```

What happens inside the `get` and `post` extractors? The framework splits the path part of the URL into elements seperated by `/` and puts them into a list. You can then use all of Scala's pattern matching features to match on and extract values from that list, helped by some special extractors defined by the framework.

See the [routing examples](https://github.com/kavedaa/vedaa-web-examples/blob/master/examples-features/src/main/scala/no/vedaadata/web/examples/routing.scala) for more examples.

### Servicers

In the examples shown so far, we have only returned a simple text response.  The general mechanism for returning a response is the `Servicer` class. Each `case` expression in a `route` block must evaluate to an instance of this class, which has one abstract method that must be implemented:

```scala
abstract class Servicer {
  def service(implicit c: ServletCycle)
}
```
Here's a minimal example:

```scala
route {

  case _ => new Servicer {
     def service(implicit c: ServletCycle) {
      c.response setContentType "text/plain"
      c.response.getWriter print "Hello world"
    }  
  }
}
```

This returns a text response exactly like the first example - in fact, they are identical, since the latter relies on an *implicit conversion* from `String` to a `Servicer` similar to the one above.

You'll likely not use the `Servicer` class directly, instead you'll inherit from one of its subclasses, or use one of the convenient factories, e.g. for XHTML:

```scala
case get(_) => Xhtml {
  <html>
    <head>
      <title>Hello world</title>
    </head>
    <body>
      <p>Hello world</p>
    </body>
  </html>
```

See the [servicers examples](https://github.com/kavedaa/vedaa-web-examples/blob/master/examples-features/src/main/scala/no/vedaadata/web/examples/servicers.scala) for more examples.

Good practice is to treat the servicer as the "view" part of a model-view-controller design, that is, to not do any processing in it but rather just use it to render data onto the response.

### Accessing the HttpServletRequest and HttpServletResponse

The request and the response are available within the servicer, but sometimes you need to access them in the router as well. You can do so with the corresponding extractors:

```scala
case get(_) & Request(request) & Response(response) => 
```
(These of course match on everything and are used only for extraction. Note that the `&` extractor is actually not part of the Scala standard library.)

### Request parameters

You can access the request parameters using `request.getParameter`, but a more powerful way is to use the  `Parameters` extractor:

```scala
  case get(_) & Parameters(params) => 
```
  
The extracted `params` value is an instance of the `Parameters` class, which implements `Map[String, String]`. You can thus access parameter values like this:

```scala
  val name = params get "name"
```

There are also methods for parsing values as specific types:

```scala
  val age = params int "age"	//	Returns None if not present OR not parsable as Int
```

For accessing multiple values for same parameters, use `params.multi`, which returns an instance of the class `MultiParameters` implementing `Map[String, Seq[String]]`. Similar access methods are available on this class.

See the [parameters examples](https://github.com/kavedaa/vedaa-web-examples/blob/master/examples-features/src/main/scala/no/vedaadata/web/examples/parameters.scala) for more examples.

### Session data

You can extract the session similarly to the parameters:

```scala
  case get(_) & Session(session) =>
```

However, working with the `getAttribute` and `setAttribute` methods of HttpSession is a bit awkward. (As in "type-unsafe" and "string-based".) A better approach is to be able to refer to typed variables in the session directly. There's a little utility class `SessionData` that lets you do that. Here's how to use it:

Define some data structure to hold your data:

```scala
class MyData {
  var name: Option[String] = None
}
```
(It's a good practice to wrap each variable in Option and initialize it to None, but not necessary. You could of course use some particular default value instead.)

Extend `SessionData`:

```scala
object MyData extends SessionData[MyData]("my-data") {
  def init = new MyData
}
```
You now have an extractor object that you can use like this:

```scala
case get(_) & Session(MyData(myData)) =>
  println(myData.name)
  myData.name = Some("John")
```

There's no need to save the data again in the session - you are working directly with data that are already in the session, stored under the attribute "my-data". (If you use several `SessionData`s in your web application, make sure to name them differently.) The data will be initialized (using the `init` method) the first time you use the extractor (i.e., when the attribute is not present in the session).

See the [session examples](https://github.com/kavedaa/vedaa-web-examples/blob/master/examples-features/src/main/scala/no/vedaadata/web/examples/session.scala) for a complete example.


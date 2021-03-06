package lila
package templating

import java.text.SimpleDateFormat
import java.util.Date
import org.apache.commons.lang3.StringEscapeUtils.escapeXml
import play.api.templates.Html
import java.util.regex.Matcher.quoteReplacement

trait StringHelper {

  def netDomain: String

  val slugify = core.String.slugify _

  def shorten(text: String, length: Int, sep: String = " [...]"): String = {
    val t = text.replace("\n", " ")
    if (t.size > (length + sep.size)) (t take length) ++ sep
    else t
  }

  def shortenWithBr(text: String, length: Int) = Html {
    nl2br(escape(text).take(length)).replace("<br /><br />", "<br />")
  }

  def pluralize(s: String, n: Int) = "%d %s%s".format(n, s, if (n > 1) "s" else "")

  def autoLink(text: String) = Html { (nl2br _ compose addLinks _ compose escape _)(text) }

  // the replace quot; -> " is required
  // to avoid issues caused by addLinks
  // when an url is surrounded by quotes
  def escape(text: String) = escapeXml(text).replace("&quot;", "\"")

  def nl2br(text: String) = text.replace("\r\n", "<br />").replace("\n", "<br />")

  private val urlRegex = """(?i)\b((?:https?://|www\d{0,3}[.]|[a-z0-9.\-]+[.][a-z]{2,4}/)(?:[^\s()<>]+|\(([^\s()<>]+|(\([^\s()<>]+\)))*\))+(?:\(([^\s()<>]+|(\([^\s()<>]+\)))*\)|[^\s`!()\[\]{};:'".,<>?«»“”‘’]))""".r

  def addLinks(text: String) = urlRegex.replaceAllIn(text, m ⇒ "<a href='%s'>%s</a>".format(
    (prependHttp _ compose delocalize _ compose quoteReplacement _)(m group 1),
    (delocalize _ compose quoteReplacement _)(m group 1)
  ))

  private def prependHttp(url: String): String = 
    url startsWith "http" fold(url, "http://" + url)

  private val delocalizeRegex = ("""\w+\.""" + quoteReplacement(netDomain)).r

  private def delocalize(url: String) = delocalizeRegex.replaceAllIn(url, netDomain)

  def showNumber(n: Int): String = (n > 0).fold("+" + n, n.toString)

  implicit def richString(str: String) = new {
    def active(other: String, then: String = "active") =
      (str == other).fold(then, "")
  }

  def strong(x: Int): String = strong(x.toString)
  def strong(x: String): String = "<strong>" + x + "</strong>"
}

package edu.umass.cs.iesl.wikilink.google

import cc.refectorie.user.sameer.util.CmdLine
import java.net.URL
import java.util.regex.Pattern
import collection.mutable.{HashSet, HashMap}

/**
 * @author sameer
 * @date 3/8/12
 */

object Stats {

  trait PageStats {
    def process(page: Webpage): Unit
  }

  object PageCounts extends PageStats {
    var num = 0

    def process(page: Webpage) {
      num += 1
    }

    override def toString = "page count: " + num
  }

  object PageTypes extends PageStats {
    val extsMap: HashMap[String, Int] = new HashMap[String, Int] {
      override def default(key: String) = 0
    }
    val pm = Pattern.compile("\\.[a-zA-Z]{2,5}$").matcher("")

    val protoMap: HashMap[String, Int] = new HashMap[String, Int] {
      override def default(key: String) = 0
    }

    def process(page: Webpage) {
      val url = new URL(page.url)
      protoMap(url.getProtocol) = protoMap(url.getProtocol) + 1
      pm.reset(url.getPath)
      if (pm.find) {
        val ext = pm.group()
        extsMap(ext) = extsMap(ext) + 1
      } else extsMap("NONE") = extsMap("NONE") + 1
    }

    override def toString = "Extensions:%s\nProtocols:%s".format(extsMap.toSeq.sortBy(_._2).mkString("\n\t", "\n\t", ""), protoMap.mkString("\n\t", "\n\t", ""))
  }

  trait MentionStats {
    def process(mention: Mention): Unit
  }

  object MentionCounts extends MentionStats {
    var num = 0
    val urls = new HashSet[String]
    val paths = new HashSet[String]
    val anchorTexts = new HashSet[String]
    val anchorLowerTexts = new HashSet[String]

    def process(m: Mention) {
      urls += m.wikiURL.replaceAll("^shttp","http")
      paths += new URL(m.wikiURL.replaceAll("^shttp","http")).getPath
      anchorTexts += m.text
      anchorLowerTexts += m.text.toLowerCase
      num += 1
    }

    override def toString = "mention count: %d\nunique wiki urls: %d\nunique wiki paths: %d\nunique anchors: %d\nunique anchors (lower): %d".format(num, urls.size, paths.size, anchorTexts.size, anchorLowerTexts.size)
  }

  val pageHooks = Seq(PageCounts, PageTypes)
  val mentionHooks = Seq(MentionCounts)

  def main(args: Array[String]) {
    val opts = CmdLine.parse(args)
    println(opts)
    val filename = opts.getOrElse("file", "/Users/sameer/tmp/input")
    val takeOnly = opts.getOrElse("take", Int.MaxValue.toString).toInt
    val iterator = new WebpageIterator(filename, takeOnly)
    for (p <- iterator) {
      pageHooks.foreach(_.process(p))
      for (m <- p.mentions) mentionHooks.foreach(_.process(m))
    }
    pageHooks.foreach(p => println(p))
    mentionHooks.foreach(m => println(m))
  }
}
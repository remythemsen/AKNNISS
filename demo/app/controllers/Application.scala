package controllers

import models.LSHS
import play.api.mvc._

class Application extends Controller {
  val lshs = new LSHS

  // Initial Index Page
  def index = Action {
    val initQs = lshs.lookupMap.take(30).toList
    Ok(views.html.index(initQs))
  }

  // Query Request
  def query(pictureId:String) = Action {
    val res = lshs.getResults(pictureId)
    Ok(views.html.query(res._2, res._1, res._2.length))
  }

}
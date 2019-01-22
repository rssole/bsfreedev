package v1.post

import java.time.LocalDateTime

import javax.inject.{Inject, Provider}
import play.api.MarkerContext

import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.json._

/**
  * DTO for displaying post information.
  */
final case class CarResource(id: String, title: String, fuel: String, price: String, mileage: String, firstRegistration: String, isNew: String)

object CarResource {

  /**
    * Mapping to write a PostResource out as a JSON value.
    */
  implicit val implicitWrites: Writes[CarResource] = (car: CarResource) => {
    Json.obj(
      "id" -> car.id,
      "title" -> car.title,
      "fuel" -> car.fuel,
      "price" -> car.price,
      "mileage" -> car.mileage,
      "firstRegistration" -> car.firstRegistration,
      "isNew" -> car.isNew
    )
  }
}

/**
  * Controls access to the backend data, returning [[CarResource]]
  */
class CarResourceHandler @Inject()(routerProvider: Provider[PostRouter],
                                   carRepository: CarRepository)(implicit ec: ExecutionContext) {

  def create(postInput: PostFormInput)(implicit mc: MarkerContext): Future[CarResource] = {
    val data = CarData(VIN("999"), postInput.title, postInput.body, 0, 0, LocalDateTime.now())
    // We don't actually create the post, so return what we have
    carRepository.create(data).map { id =>
      createCarResource(data)
    }
  }

  def lookup(id: String)(implicit mc: MarkerContext): Future[Option[CarResource]] = {
    val postFuture = carRepository.get(VIN(id))
    postFuture.map { maybePostData =>
      maybePostData.map { postData =>
        createCarResource(postData)
      }
    }
  }

  def find(implicit mc: MarkerContext): Future[Iterable[CarResource]] = {
    carRepository.list().map { postDataList =>
      postDataList.map(postData => createCarResource(postData))
    }
  }

  private def createCarResource(p: CarData): CarResource = {
    CarResource(p.id.toString, routerProvider.get.link(p.id), p.title, p.fuel)
  }

}

package v1.post

import java.time.LocalDate

import akka.actor.ActorSystem
import javax.inject.{Inject, Singleton}
import play.api.libs.concurrent.CustomExecutionContext
import play.api.{Logger, MarkerContext}
import v1.post.FuelType.FuelType

import scala.concurrent.Future

final case class CarData(id: VIN, title: String, fuel: FuelType, price: Int, mileage: Int, firstRegistration: LocalDate = LocalDate.now(), isNew: Boolean)

/**
  * Vehicle Identification Number
  * @param underlying Actual value contained
  */
class VIN private(val underlying: Int) extends AnyVal {
  override def toString: String = underlying.toString
}

object VIN {
  def apply(raw: String): VIN = {
    require(raw != null)
    new VIN(Integer.parseInt(raw))
  }
}


class PostExecutionContext @Inject()(actorSystem: ActorSystem) extends CustomExecutionContext(actorSystem, "repository.dispatcher")

/**
  * A pure non-blocking interface for the PostRepository.
  */
trait CarRepository {
  def create(data: CarData)(implicit mc: MarkerContext): Future[VIN]

  def list()(implicit mc: MarkerContext): Future[Iterable[CarData]]

  def get(id: VIN)(implicit mc: MarkerContext): Future[Option[CarData]]
}

/**
  * A trivial implementation for the Car Repository.
  *
  * A custom execution context is used here to establish that blocking operations should be
  * executed in a different thread than Play's ExecutionContext, which is used for CPU bound tasks
  * such as rendering.
  */
@Singleton
class CarRepositoryImpl @Inject()()(implicit ec: PostExecutionContext) extends CarRepository {

  private val logger = Logger(this.getClass)

  private val postList = List(
    CarData(VIN("1"), "title 1", FuelType.Gasoline, 0, 0, LocalDate.now(), false),
    CarData(VIN("2"), "title 2", FuelType.Gasoline, 0, 0, LocalDate.now(), false),
    CarData(VIN("3"), "title 3", FuelType.Diesel, 0, 0, LocalDate.now(), false),
    CarData(VIN("4"), "title 4", FuelType.Gasoline, 0, 0, LocalDate.now(), false),
    CarData(VIN("5"), "title 5", FuelType.Diesel, 0, 0, LocalDate.now(), false)
  )

  override def list()(implicit mc: MarkerContext): Future[Iterable[CarData]] = {
    Future {
      logger.trace(s"list: ")
      postList
    }
  }

  override def get(id: VIN)(implicit mc: MarkerContext): Future[Option[CarData]] = {
    Future {
      logger.trace(s"get: id = $id")
      postList.find(car => car.id == id)
    }
  }

  def create(car: CarData)(implicit mc: MarkerContext): Future[VIN] = {
    Future {
      logger.trace(s"create: data = $car")
      car.id
    }
  }

}

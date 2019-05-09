package domain

import java.time.{DayOfWeek, LocalTime}
import java.util.UUID

case class Profile(id: UUID, account: Account, firstName: String, lastName: String, drives: List[Drive])

case class Drive(id: UUID, lookupCode: LookupCode, address: Address, availability: List[Available])

case class LookupCode(value: String)

case class Address(firstLine: String, secondLine: String, county: String, postcode: String)

case class Available(day: DayOfWeek, from: LocalTime, to: LocalTime)


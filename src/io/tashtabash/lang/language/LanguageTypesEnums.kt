package io.tashtabash.lang.language

import io.tashtabash.random.SampleSpaceObject


// Other values are possible and can be generated after choosing one of these start options.
enum class VowelQualityAmount(val amount: Int, override val probability: Double) : SampleSpaceObject {
    Two(2, 4.0),
    Three(3, 30.0),//No actual data
    Four(4, 49.0),//No actual data
    Five(5, 188.0),
    Six(6, 100.0),
    Seven(7, 60.0),//No actual data
    Eight(8, 31.0),//No actual data
    Nine(9, 20.0),//No actual data
    Ten(10, 10.0),//No actual data
}

enum class NumeralSystemBase(override val probability: Double) : SampleSpaceObject {
    Decimal(125.0),
    Vigesimal(20.0),
    VigesimalTill100(22.0),
    SixtyBased(5.0),
//    ExtendedBodyPartSystem(4.0),
    Restricted3(5.0),
    Restricted5(5.0),
    Restricted10(5.0),
    Restricted20(5.0)
}

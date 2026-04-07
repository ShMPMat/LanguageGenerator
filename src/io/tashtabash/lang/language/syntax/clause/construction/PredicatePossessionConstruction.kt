package io.tashtabash.lang.language.syntax.clause.construction

import io.tashtabash.lang.language.syntax.SyntaxRelation
import io.tashtabash.lang.language.syntax.clause.description.MainObjectType
import io.tashtabash.lang.language.syntax.clause.description.NominalDescription
import io.tashtabash.lang.language.syntax.clause.description.ObliquePredicatePossessionDescription
import io.tashtabash.lang.language.syntax.clause.description.PossessorDescription
import io.tashtabash.lang.language.syntax.clause.description.UnfoldableClauseDescription
import io.tashtabash.lang.language.syntax.clause.description.VerbDescription
import io.tashtabash.lang.language.syntax.clause.description.VerbMainClauseDescription


interface PredicatePossessionConstruction : Construction {
    fun apply(owner: NominalDescription, owned: NominalDescription): UnfoldableClauseDescription

    object HaveVerb : PredicatePossessionConstruction {
        override fun apply(owner: NominalDescription, owned: NominalDescription) = VerbMainClauseDescription(
            VerbDescription(
                "have",
                mapOf(MainObjectType.Agent to owner, MainObjectType.Patient to owned)
            )
        )
    }

    object GenitiveOblique : PredicatePossessionConstruction {
        override fun apply(owner: NominalDescription, owned: NominalDescription) = VerbMainClauseDescription(
            VerbDescription(
                "exist",
                mapOf(MainObjectType.Argument to owned.copyAndAddDefinitions(PossessorDescription(owner)))
            )
        )
    }

    object LocativeOblique : PredicatePossessionConstruction {
        override fun apply(owner: NominalDescription, owned: NominalDescription) =
            ObliquePredicatePossessionDescription(owner, owned, SyntaxRelation.Location)
    }

    object DativeOblique : PredicatePossessionConstruction {
        override fun apply(owner: NominalDescription, owned: NominalDescription) =
            ObliquePredicatePossessionDescription(owner, owned, SyntaxRelation.Addressee)
    }

    object Topic : PredicatePossessionConstruction {
        override fun apply(owner: NominalDescription, owned: NominalDescription) =
            ObliquePredicatePossessionDescription(owner, owned, SyntaxRelation.Topic)
    }
}

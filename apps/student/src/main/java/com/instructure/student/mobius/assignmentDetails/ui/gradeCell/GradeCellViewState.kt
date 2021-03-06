/*
 * Copyright (C) 2019 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.student.mobius.assignmentDetails.ui.gradeCell

import android.content.Context
import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.utils.NumberHelper
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.student.R

sealed class GradeCellViewState {
    object Empty : GradeCellViewState()
    object Submitted : GradeCellViewState()
    data class GradeData(
        val showCompleteIcon: Boolean = false,
        val showIncompleteIcon: Boolean = false,
        val showPointsLabel: Boolean = false,
        @ColorInt val accentColor: Int = Color.GRAY,
        val graphPercent: Float = 0f,
        val score: String = "",
        val grade: String = "",
        val outOf: String = "",
        val outOfContentDescription: String = "",
        val latePenalty: String = "",
        val finalGrade: String = ""
    ) : GradeCellViewState()

    companion object {
        /**
         * Generates a [GradeCellViewState] from the provided [submission] and [assignment]. The [submission] must be
         * a root submission, meaning it should be the parent submission object rather than an object from the
         * submission history.
         */
        fun fromSubmission(
            context: Context,
            assignment: Assignment,
            submission: Submission?
        ): GradeCellViewState {
            // Return empty state for missing/null submission, unsubmitted workflow state, or "Not Graded" grading type
            if (submission?.workflowState == null
                || submission.workflowState == "unsubmitted"
                || assignment.gradingType == Assignment.NOT_GRADED_TYPE) {
                return Empty
            }

            // Return the Submitted state if the submission has not been graded
            if (submission.workflowState == "submitted" && !submission.isGraded) return Submitted

            /* The accent color, which determines the graph color and color of the late penalty text, should match
             * the course color. The only exception to this is when the submission for a Complete/Incomplete assignment
             * is graded as Incomplete, in which case the accent color should match the gray text color */
            val accentColor = ColorKeeper.getOrGenerateColor("course_${assignment.courseId}")

            /* The 'Out of' text abbreviates the word "points" to "pts" which is read as "P T S" by screen readers, so
             * we use a second string with the full word "points" as a content description. */
            val pointsPossibleText = NumberHelper.formatDecimal(assignment.pointsPossible, 2, true)
            val outOfText = context.getString(R.string.outOfPointsAbbreviatedFormatted, pointsPossibleText)
            val outOfContentDescriptionText = context.getString(R.string.outOfPointsFormatted, pointsPossibleText)

            // Excused
            if (submission.excused) {
                return GradeData(
                    graphPercent = 1.0f,
                    showCompleteIcon = true,
                    accentColor = accentColor,
                    grade = context.getString(R.string.gradeExcused),
                    outOf = outOfText,
                    outOfContentDescription = outOfContentDescriptionText
                )
            }

            // Complete/Incomplete
            if (assignment.gradingType == Assignment.PASS_FAIL_TYPE) {
                val isComplete = (submission.grade == "complete")
                return GradeData(
                    showCompleteIcon = isComplete,
                    showIncompleteIcon = !isComplete,
                    grade = context.getString(if (isComplete) R.string.gradeComplete else R.string.gradeIncomplete),
                    accentColor = if (isComplete) accentColor else ContextCompat.getColor(context, R.color.defaultTextGray),
                    outOf = outOfText,
                    outOfContentDescription = outOfContentDescriptionText,
                    graphPercent = 1.0f
                )
            }

            val score = NumberHelper.formatDecimal(submission.enteredScore, 2, true)
            val graphPercent = (submission.enteredScore / assignment.pointsPossible).coerceIn(0.0, 1.0).toFloat()

            // If grading type is Points, don't show the grade since we're already showing it as the score
            var grade = if (assignment.gradingType != Assignment.POINTS_TYPE) submission.grade.orEmpty() else ""

            var latePenalty = ""
            var finalGrade = ""

            // Adjust for late penalty, if any
            if (submission.pointsDeducted ?: 0.0 > 0.0) {
                grade = "" // Grade will be shown in the 'final grade' text
                val pointsDeducted = NumberHelper.formatDecimal(submission.pointsDeducted!!, 2, true)
                latePenalty = context.getString(R.string.latePenalty, pointsDeducted)
                finalGrade = context.getString(R.string.finalGradeFormatted, submission.grade)
            }

            return GradeCellViewState.GradeData(
                graphPercent = graphPercent,
                accentColor = accentColor,
                score = score,
                showPointsLabel = true,
                outOf = outOfText,
                outOfContentDescription = outOfContentDescriptionText,
                grade = grade,
                latePenalty = latePenalty,
                finalGrade = finalGrade
            )
        }
    }
}

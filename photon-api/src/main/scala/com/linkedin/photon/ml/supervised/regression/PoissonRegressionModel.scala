/*
 * Copyright 2017 LinkedIn Corp. All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.linkedin.photon.ml.supervised.regression

import breeze.linalg.Vector
import org.apache.spark.rdd.RDD

import com.linkedin.photon.ml.TaskType._
import com.linkedin.photon.ml.model.Coefficients
import com.linkedin.photon.ml.supervised.model.GeneralizedLinearModel

/**
 * Class for the classification model trained using Poisson Regression.
 *
 * @param coefficients Weights estimated for every feature
 */
class PoissonRegressionModel(override val coefficients: Coefficients)
  extends GeneralizedLinearModel(coefficients)
  with Regression
  with Serializable {

  /**
   *
   * @return The model type
   */
  override def modelType: TaskType = POISSON_REGRESSION

  /**
   * Compute the mean response of the Poisson regression model.
   *
   * @param features The input data point's feature
   * @param offset The input data point's offset
   * @return The mean for the passed features
   */
  override protected[ml] def computeMean(features: Vector[Double], offset: Double): Double =
    math.exp(coefficients.computeScore(features) + offset)

  /**
   *
   * @param updatedCoefficients
   * @return A new generalized linear model with the passed coefficients
   */
  override def updateCoefficients(updatedCoefficients: Coefficients): PoissonRegressionModel =
    new PoissonRegressionModel(updatedCoefficients)

  /**
   * Method used to define equality on multiple class levels while conforming to equality contract. Defines under
   * what circumstances this class can equal another class.
   *
   * @param other Some object
   * @return Whether this object can equal the other object
   */
  override def canEqual(other: Any): Boolean = other.isInstanceOf[PoissonRegressionModel]

  /**
   *
   * @return A summary of the object in string representation
   */
  override def toSummaryString: String =
    s"Poisson Regression Model with the following coefficients:\n${coefficients.toSummaryString}"

  /**
   *
   * @param features vector representing feature of a single data point's features
   * @param offset offset of the data point
   * @return Double prediction from the trained model
   */
  override def predictWithOffset(features: Vector[Double], offset: Double): Double =
    computeMeanFunctionWithOffset(features, offset)

  /**
   *
   * @param featuresWithOffsets data points of the form RDD[(feature, offset)]
   * @return RDD[Double] where each entry contains the corresponding prediction
   */
  override def predictAllWithOffsets(featuresWithOffsets: RDD[(Vector[Double], Double)]): RDD[Double] =
    GeneralizedLinearModel.computeMeanFunctionsWithOffsets(this, featuresWithOffsets)
}

object PoissonRegressionModel {
  /**
   * Create a new Poisson regression model with the provided coefficients (means) and variances.
   *
   * @param coefficients The feature coefficient means and variances for the model
   * @return A Poisson regression model
   */
  def apply(coefficients: Coefficients): PoissonRegressionModel = new PoissonRegressionModel(coefficients)
}

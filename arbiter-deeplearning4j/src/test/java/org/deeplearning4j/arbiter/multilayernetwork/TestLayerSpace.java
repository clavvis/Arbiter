/*-
 *
 *  * Copyright 2016 Skymind,Inc.
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *        http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 *
 */
package org.deeplearning4j.arbiter.multilayernetwork;

import org.deeplearning4j.arbiter.conf.updater.SgdSpace;
import org.deeplearning4j.arbiter.layers.*;
import org.deeplearning4j.arbiter.optimize.api.ParameterSpace;
import org.deeplearning4j.arbiter.optimize.parameter.continuous.ContinuousParameterSpace;
import org.deeplearning4j.arbiter.optimize.parameter.discrete.DiscreteParameterSpace;
import org.deeplearning4j.arbiter.optimize.parameter.integer.IntegerParameterSpace;
import org.deeplearning4j.nn.conf.layers.*;
import org.junit.Test;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.activations.IActivation;
import org.nd4j.linalg.learning.config.Sgd;

import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestLayerSpace {

    @Test
    public void testBasic1() {

        DenseLayer expected = new DenseLayer.Builder().nOut(13).activation(Activation.RELU).build();

        DenseLayerSpace space = new DenseLayerSpace.Builder().nOut(13).activation(Activation.RELU).build();

        int nParam = space.numParameters();
        assertEquals(0, nParam);
        DenseLayer actual = space.getValue(new double[nParam]);

        assertEquals(expected, actual);
    }

    @Test
    public void testBasic2() {

        Activation[] actFns = new Activation[] {Activation.SOFTSIGN, Activation.RELU, Activation.LEAKYRELU};
        Random r = new Random(12345);

        for (int i = 0; i < 20; i++) {

            new DenseLayer.Builder().build();

            DenseLayerSpace ls =
                            new DenseLayerSpace.Builder().nOut(20)
                                    .updater(new SgdSpace(new ContinuousParameterSpace(0.3, 0.4)))
                                            .l2(new ContinuousParameterSpace(0.01, 0.1))
                                            .activation(new DiscreteParameterSpace<>(actFns)).build();

            //Set the parameter numbers...
            List<ParameterSpace> list = ls.collectLeaves();
            int k = 0;
            for (int j = 0; j < list.size(); j++) {
                if (list.get(j).numParameters() > 0) {
                    list.get(j).setIndices(k++);
                }
            }

            int nParam = ls.numParameters();
            assertEquals(3, nParam);

            double[] d = new double[nParam];
            for (int j = 0; j < d.length; j++) {
                d[j] = r.nextDouble();
            }

            DenseLayer l = ls.getValue(d);

            assertEquals(20, l.getNOut());
            double lr = ((Sgd)l.getIUpdater()).getLearningRate();
            double l2 = l.getL2();
            IActivation activation = l.getActivationFn();

            System.out.println(lr + "\t" + l2 + "\t" + activation);

            assertTrue(lr >= 0.3 && lr <= 0.4);
            assertTrue(l2 >= 0.01 && l2 <= 0.1);
            assertTrue(containsActivationFunction(actFns, activation));
        }
    }

    @Test
    public void testBatchNorm() {
        BatchNormalizationSpace sp = new BatchNormalizationSpace.Builder().gamma(1.5)
                        .beta(new ContinuousParameterSpace(2, 3)).lockGammaBeta(true).build();

        //Set the parameter numbers...
        List<ParameterSpace> list = sp.collectLeaves();
        int k = 0;
        for (int j = 0; j < list.size(); j++) {
            if (list.get(j).numParameters() > 0) {
                list.get(j).setIndices(k++);
            }
        }

        BatchNormalization bn = sp.getValue(new double[] {0.6});
        assertTrue(bn.isLockGammaBeta());
        assertEquals(1.5, bn.getGamma(), 0.0);
        assertEquals(0.6 * (3 - 2) + 2, bn.getBeta(), 1e-4);
    }

    @Test
    public void testActivationLayer() {
        Activation[] actFns = new Activation[] {Activation.SOFTSIGN, Activation.RELU, Activation.LEAKYRELU};

        ActivationLayerSpace als =
                        new ActivationLayerSpace.Builder().activation(new DiscreteParameterSpace<>(actFns)).build();
        //Set the parameter numbers...
        List<ParameterSpace> list = als.collectLeaves();
        for (int j = 0; j < list.size(); j++) {
            list.get(j).setIndices(j);
        }

        int nParam = als.numParameters();
        assertEquals(1, nParam);

        Random r = new Random(12345);

        for (int i = 0; i < 20; i++) {

            double[] d = new double[nParam];
            for (int j = 0; j < d.length; j++) {
                d[j] = r.nextDouble();
            }

            ActivationLayer al = als.getValue(d);
            IActivation activation = al.getActivationFn();

            System.out.println(activation);

            assertTrue(containsActivationFunction(actFns, activation));
        }
    }

    @Test
    public void testEmbeddingLayer() {

        Activation[] actFns = new Activation[] {Activation.SOFTSIGN, Activation.RELU, Activation.LEAKYRELU};

        EmbeddingLayerSpace els = new EmbeddingLayerSpace.Builder().activation(new DiscreteParameterSpace<>(actFns))
                        .nIn(10).nOut(new IntegerParameterSpace(10, 20)).build();
        //Set the parameter numbers...
        List<ParameterSpace> list = els.collectLeaves();
        int k = 0;
        for (int j = 0; j < list.size(); j++) {
            if (list.get(j).numParameters() > 0) {
                list.get(j).setIndices(k++);
            }
        }

        int nParam = els.numParameters();
        assertEquals(2, nParam);

        Random r = new Random(12345);

        for (int i = 0; i < 20; i++) {

            double[] d = new double[nParam];
            for (int j = 0; j < d.length; j++) {
                d[j] = r.nextDouble();
            }

            EmbeddingLayer el = els.getValue(d);
            IActivation activation = el.getActivationFn();
            int nOut = el.getNOut();

            System.out.println(activation + "\t" + nOut);

            assertTrue(containsActivationFunction(actFns, activation));
            assertTrue(nOut >= 10 && nOut <= 20);
        }
    }

    @Test
    public void testGravesBidirectionalLayer() {

        Activation[] actFns = new Activation[] {Activation.SOFTSIGN, Activation.RELU, Activation.LEAKYRELU};

        GravesBidirectionalLSTMLayerSpace ls =
                        new GravesBidirectionalLSTMLayerSpace.Builder().activation(new DiscreteParameterSpace<>(actFns))
                                        .forgetGateBiasInit(new ContinuousParameterSpace(0.5, 0.8)).nIn(10)
                                        .nOut(new IntegerParameterSpace(10, 20)).build();
        //Set the parameter numbers...
        List<ParameterSpace> list = ls.collectLeaves();
        int k = 0;
        for (int j = 0; j < list.size(); j++) {
            if (list.get(j).numParameters() > 0) {
                list.get(j).setIndices(k++);
            }
        }

        int nParam = ls.numParameters();
        assertEquals(3, nParam); //Excluding fixed value for nIn

        Random r = new Random(12345);

        for (int i = 0; i < 20; i++) {

            double[] d = new double[nParam];
            for (int j = 0; j < d.length; j++) {
                d[j] = r.nextDouble();
            }

            GravesBidirectionalLSTM el = ls.getValue(d);
            IActivation activation = el.getActivationFn();
            int nOut = el.getNOut();
            double forgetGate = el.getForgetGateBiasInit();

            System.out.println(activation + "\t" + nOut + "\t" + forgetGate);

            assertTrue(containsActivationFunction(actFns, activation));
            assertTrue(nOut >= 10 && nOut <= 20);
            assertTrue(forgetGate >= 0.5 && forgetGate <= 0.8);
        }
    }

    private static boolean containsActivationFunction(Activation[] activationFunctions,
                    IActivation activationFunction) {
        for (Activation af : activationFunctions) {
            if (activationFunction.equals(af.getActivationFunction()))
                return true;
        }
        return false;
    }
}

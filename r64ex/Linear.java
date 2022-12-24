package com.mathpar.NAUKMA.MAG21.zhukovskyi.r64ex;

import com.mathpar.matrix.MatrixS;
import com.mathpar.number.NumberR64;
import com.mathpar.number.Ring;
import mpi.MPI;
import mpi.MPIException;

import java.io.IOException;
import java.util.Random;

public class Linear {

    public static void main(String[] args) throws MPIException, IOException, ClassNotFoundException {

        int ord = Integer.parseInt(args[0]);
        int den = Integer.parseInt(args[1]);

        Ring ring = new Ring("R64[]");

        Random rnd = new Random(0);


        MPI.Init(args);

        int rank = MPI.COMM_WORLD.getRank();

        if(rank==0) {

            MatrixS A = new MatrixS(ord, ord, den,
                    new int[]{5}, rnd, NumberR64.ONE, ring);

            MatrixS B = new MatrixS(ord, ord, den,
                    new int[]{5}, rnd, NumberR64.ONE, ring);

            long startTime = System.nanoTime();

            MatrixS D = A.multiply(B, ring);

            long endTime = System.nanoTime();

            System.out.println("Time to calc (ms): " + (endTime - startTime) / 1000000);

          //  System.out.println(D);

        }

        MPI.Finalize();


    }


}


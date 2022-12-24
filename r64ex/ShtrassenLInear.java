package com.mathpar.NAUKMA.MAG21.zhukovskyi.r64ex;

import com.mathpar.matrix.MatrixS;
import com.mathpar.number.NumberR64;
import com.mathpar.number.Ring;
import com.mathpar.parallel.utils.MPITransport;
import mpi.MPI;
import mpi.MPIException;

import java.io.IOException;
import java.util.Random;

public class ShtrassenLInear {
    public static void main(String[] args) throws MPIException, IOException, ClassNotFoundException {

        Ring ring = new Ring("R64[]");
        MPI.Init(args);
        int tag = 0;
        int rank = MPI.COMM_WORLD.getRank();
        if (rank == 0) {
            int ord = Integer.parseInt(args[0]);
            int den = Integer.parseInt(args[1]);
            Random rnd = new Random(0);
            MatrixS A = new MatrixS(ord, ord, den, new int[]{5}, rnd, NumberR64.ONE, ring);
//            System.out.println("A = " + A);
            MatrixS B = new MatrixS(ord, ord, den, new int[]{5}, rnd, NumberR64.ONE, ring);
//            System.out.println("B = " + B);

            long startTime = System.nanoTime();

            MatrixS[] AA = A.split();
            MatrixS[] BB = B.split();

            MatrixS[] DD = new MatrixS[4];

            MatrixS m1 = (AA[0].add(AA[3], ring)).multiply(BB[0].add(BB[3], ring), ring);
            MatrixS m2 = (AA[2].add(AA[3], ring)).multiply(BB[0],ring);
            MatrixS m3 = (AA[0].multiply(BB[1].subtract(BB[3],ring),ring));
            MatrixS m4 = (AA[3].multiply(BB[2].subtract(BB[0],ring),ring));
            MatrixS m5 = AA[0].add(AA[1], ring).multiply(BB[0],ring);

            MatrixS m6 = AA[2].subtract(AA[1], ring).multiply(BB[0].add(BB[1],ring),ring);
            MatrixS m7 = AA[1].subtract(AA[3], ring).multiply(BB[1].add(BB[3],ring),ring);

            DD[0] = m1.add(m4, ring).subtract(m5, ring).add(m7, ring);
            DD[1] = m3.add(m5, ring);
            DD[2] = m2.add(m4, ring);
            DD[3] = m1.subtract(m2, ring).add(m3, ring).add(m6, ring);

            MatrixS CC = MatrixS.join(DD);

            long endTime = System.nanoTime();
            System.out.println("Time to calc (ms): " + (endTime - startTime)/1000000);

//            System.out.println("C = " + CC);
        }
        MPI.Finalize();
    }
}
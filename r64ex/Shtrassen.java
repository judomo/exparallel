package com.mathpar.NAUKMA.MAG21.zhukovskyi.r64ex;

import com.mathpar.matrix.MatrixS;
import com.mathpar.number.NumberR64;
import com.mathpar.number.Ring;
import com.mathpar.parallel.utils.MPITransport;
import mpi.MPI;
import mpi.MPIException;

import java.io.IOException;
import java.util.Random;

public class Shtrassen {
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

            MPITransport.sendObjectArray(new Object[]{AA[2], AA[3], BB[0]}, 0, 3, 1, 1);
            MPITransport.sendObjectArray(new Object[]{AA[0], BB[1], BB[3]}, 0, 3, 2, 2);
            MPITransport.sendObjectArray(new Object[]{AA[3], BB[2], BB[0]}, 0, 3, 3, 3);
            MPITransport.sendObjectArray(new Object[]{AA[0], AA[1], BB[3]}, 0, 3, 4, 4);
            MPITransport.sendObjectArray(new Object[]{AA[2], AA[0], BB[0], BB[1]}, 0, 4, 5, 5);
            MPITransport.sendObjectArray(new Object[]{AA[1], AA[3], BB[2], BB[3]}, 0, 4, 6, 6);

            MatrixS m1 = (AA[0].add(AA[3], ring)).multiply(BB[0].add(BB[3], ring), ring);
            MatrixS m2 = (MatrixS) MPITransport.recvObject(1, tag);
            MatrixS m3 = (MatrixS) MPITransport.recvObject(2, tag);
            MatrixS m4 = (MatrixS) MPITransport.recvObject(3, tag);
            MatrixS m5 = (MatrixS) MPITransport.recvObject(4, tag);
            MatrixS m6 = (MatrixS) MPITransport.recvObject(5, tag);
            MatrixS m7 = (MatrixS) MPITransport.recvObject(6, tag);

            DD[0] = m1.add(m4, ring).subtract(m5, ring).add(m7, ring);
            DD[1] = m3.add(m5, ring);
            DD[2] = m2.add(m4, ring);
            DD[3] = m1.subtract(m2, ring).add(m3, ring).add(m6, ring);

            MatrixS CC = MatrixS.join(DD);

            long endTime = System.nanoTime();

            System.out.println("Time to calc (ms): " + (endTime - startTime)/1000000);

        } else {
            Object[] b;
            MatrixS[] a;
            MatrixS res = null;
            if (rank >= 1 && rank <= 4) {
                b = new Object[3];
                a = new MatrixS[3];
                MPITransport.recvObjectArray(b, 0, 3, 0, rank);
                for (int i = 0; i < b.length; i++)
                    a[i] = (MatrixS) b[i];

                switch (rank) {
                    case 1:
                        res = a[0].add(a[1], ring).multiply(a[2], ring);
                        break;
                    case 2:
                        res = a[0].multiply(a[1].subtract(a[2],ring), ring);
                        break;
                    case 3:
                        res = a[0].multiply(a[1].subtract(a[2],ring), ring);
                        break;
                    case 4:
                        res = a[0].add(a[1], ring).multiply(a[2], ring);
                        break;
                }

            } else {
                b = new Object[4];
                a = new MatrixS[4];
                MPITransport.recvObjectArray(b, 0, 4, 0, rank);
                for (int i = 0; i < b.length; i++)
                    a[i] = (MatrixS) b[i];
                res = (a[0].subtract(a[1], ring)).multiply((a[2].add(a[3], ring)), ring);
            }

            MPITransport.sendObject(res, 0, tag);
        }

        MPI.Finalize();
    }
}
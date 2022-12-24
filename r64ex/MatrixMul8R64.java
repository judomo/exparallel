package com.mathpar.NAUKMA.MAG21.zhukovskyi.r64ex;

import com.mathpar.matrix.MatrixS;
import com.mathpar.number.NumberR64;
import com.mathpar.number.Ring;
import com.mathpar.parallel.utils.MPITransport;
import mpi.MPI;
import mpi.MPIException;

import java.io.IOException;
import java.util.Random;

class MatrixMul8R64 {
    public static void main(String[] args)throws MPIException, IOException,ClassNotFoundException
    {
        Ring ring = new Ring("R64[]");
        MPI.Init(args);
        int rank = MPI.COMM_WORLD.getRank();
        if (rank == 0) {
            // програма виконується на нульовому процесорi
            int ord = Integer.parseInt(args[0]);
            int den = Integer.parseInt(args[1]);
            Random rnd = new Random();
            // ord = розмiр матрицi, den = щiльнiсть
            MatrixS A = new MatrixS(ord, ord, den, new int[] {5}, rnd, NumberR64.ONE, ring);

            MatrixS B = new MatrixS(ord, ord, den, new int[] {5}, rnd, NumberR64.ONE, ring);

            MatrixS D = null;
            // розбиваємо матрицю A на 4 частини
            MatrixS[] AA = A.split();
            // розбиваємо матрицю B на 4 частини
            MatrixS[] BB = B.split();
            int tag = 0;
            long startTime = System.nanoTime();

            //розподіляємо частини матриць між процесорами
            MPITransport.sendObjectArray(new Object[] {AA[1], BB[2]},0,2, 1, tag);
            MPITransport.sendObjectArray(new Object[] {AA[0], BB[1]},0,2, 2, tag);
            MPITransport.sendObjectArray(new Object[] {AA[1], BB[3]},0,2, 3, tag);
            MPITransport.sendObjectArray(new Object[] {AA[2], BB[0]},0,2, 4, tag);
            MPITransport.sendObjectArray(new Object[] {AA[3], BB[2]},0,2, 5, tag);
            MPITransport.sendObjectArray(new Object[] {AA[2], BB[1]},0,2, 6, tag);
            MPITransport.sendObjectArray(new Object[] {AA[3], BB[3]},0,2, 7, tag);
            MatrixS[] DD = new MatrixS[4];
            //збираємо результати
            DD[0] = (AA[0].multiply(BB[0], ring)).add((MatrixS) MPITransport.recvObject(1, 3),ring);
            DD[1] = (MatrixS) MPITransport.recvObject(2, 3);
            DD[2] = (MatrixS) MPITransport.recvObject(4, 3);
            DD[3] = (MatrixS) MPITransport.recvObject(6, 3);
            D = MatrixS.join(DD);

            long endTime = System.nanoTime();

            System.out.println("Time to calc (ms): " + (endTime - startTime)/1000000);

        }
        else
        {

            Object[] b = new Object[2];
            MPITransport.recvObjectArray(b,0,2,0, 0);
            MatrixS[] a = new MatrixS[b.length];
            for (int i = 0; i < b.length; i++)
                a[i] = (MatrixS) b[i];
            MatrixS res = a[0].multiply(a[1], ring);
            //здійснюємо додавання на парних процесорах та відправляємо результат на 0
            if (rank % 2 == 0)
            {
                MatrixS p = res.add((MatrixS) MPITransport.recvObject(rank + 1, 3), ring);
                MPITransport.sendObject(p, 0, 3);
            }
            else
            {
                MPITransport.sendObject(res, rank - 1, 3);
            }
        }
        MPI.Finalize();
    }
}
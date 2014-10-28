package com.wizardfight.recognition;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Serializable;
import java.util.ArrayList;

public class MatrixDouble implements Serializable {

    private static final long serialVersionUID = 5L;
    int rows; // /< The number of rows in the Matrix
    int cols; // /< The number of columns in the Matrix
    private int capacity; // /< The actual capacity of the Matrix, this will
    double[][] dataPtr; // /< A pointer to the data

    public MatrixDouble() {
        rows = 0;
        cols = 0;
        capacity = 0;
        dataPtr = null;
    }

    public MatrixDouble(int rows, int cols) {
        dataPtr = null;
        resize(rows, cols);
    }

    /**
     * Gets a row vector [1 cols] from the Matrix at the row index r
     *
     * @param int r: the index of the row, this should be in the range [0 *
     * rows-1]
     * @return returns a row vector from the Matrix at the row index r
     */
    public ArrayList<Double> getRowVector(int r) {
        ArrayList<Double> rowVector = new ArrayList<Double>(cols);
        for (int c = 0; c < cols; c++) {
            rowVector.add(c, dataPtr[r][c]);
        }
        return rowVector;
    }

    /**
     * Gets the ranges (min and max values) of each column in the matrix.
     *
     * @return a vector with the ranges (min and max values) of each column in
     * the matrix
     */
    public ArrayList<MinMax> getRanges() {
        if (rows == 0) {
            return new ArrayList<MinMax>();
        }
        ArrayList<MinMax> ranges = new ArrayList<MinMax>(cols);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                ranges.add(new MinMax());
                ranges.get(j).updateMinMax(dataPtr[i][j]);
            }
        }
        return ranges;
    }

    public void clear() {
        rows = 0;
        cols = 0;
        dataPtr = null;
    }

    /**
     * Adds the input sample to the end of the Matrix, extending the number of
     * rows by 1. The number of columns in the sample must match the number of
     * columns in the Matrix, unless the Matrix size has not been set, in which
     * case the new sample size will define the number of columns in the Matrix.
     *
     * @param ArrayList
     * <Double> sample: the new column vector you want to add to the end of the
     * Matrix. Its size should match the number of columns in the Matrix
     * @return returns true or false, indicating if the push was successful
     */
    public boolean push_back(ArrayList<Double> sample) {
        // If there is no data, but we know how many cols are in a sample then
        // we simply create a new buffer of size 1 and add the sample
        if (dataPtr == null) {
            cols = (int) sample.size();
            if (!resize(1, cols)) {
                clear();
                return false;
            }
            for (int j = 0; j < cols; j++) {
                dataPtr[0][j] = sample.get(j);
            }
            return true;
        }

        // If there is data and the sample size does not match the number of
        // columns then return false
        if (sample.size() != cols) {
            return false;
        }

        // Check to see if we have reached the capacity, if not then simply add
        // the new data
        if (rows < capacity) {
            // Add the new sample at the end
            for (int j = 0; j < cols; j++) {
                dataPtr[rows][j] = sample.get(j);
            }

        } else { // Otherwise we copy the existing data from the data ptr into a
            // new buffer of size (rows+1) and add the sample at the end
            double[][] tempDataPtr = new double[rows + 1][cols];

            // Copy the original data
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    tempDataPtr[i][j] = dataPtr[i][j];
                }
            }

            // Add the new sample at the end
            for (int j = 0; j < cols; j++) {
                tempDataPtr[rows][j] = sample.get(j);
            }

            dataPtr = tempDataPtr;

            // Increment the capacity so it matches the number of rows
            capacity++;
        }

        // Increment the number of rows
        rows++;

        // Finally return true to signal that the data was added correctly
        return true;
    }

    /**
     * Resizes the MatrixDouble to the new size of [rows cols]
     *
     * @param int rows: the number of rows, must be greater than zero
     * @param int cols: the number of columns, must be greater than zero
     * @return returns true or false, indicating if the resize was successful
     */
    public boolean resize(int r, int c) {
        // If the rows and cols are unchanged then do not resize the data
        if (r == rows && c == cols) {
            return true;
        }

        if (r > 0 && c > 0) {
            rows = r;
            cols = c;
            capacity = r;
            dataPtr = new double[rows][];

            // Check to see if the memory was created correctly
            for (int i = 0; i < rows; i++) {
                dataPtr[i] = new double[cols];
            }
            return true;
        }
        return false;
    }
}

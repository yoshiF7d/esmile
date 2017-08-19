package application;

public class Array2D {
	double[][] elem;
	int row;
	int column;
	public Array2D(int row, int column) {
		this.row = row;
		this.column = column;
		elem = new double[row][column];
	}
	public Array2D(double[][] elem) {
		this.row = elem.length;
		this.column = elem[0].length;
		elem = new double[row][column];
	}
	private void swapRow(int i1, int i2) {
		for(int j=0;j<column;j++) {
			double temp = elem[i1][j];
			elem[i1][j] = elem[i2][j];
			elem[i2][j] = temp;
		}
	}
	private void pivot(int k) {
		int imax = k;
		double max = elem[k][k];
		for(int i=k+1;i<row;i++) {
			if(Math.abs(elem[i][k]) > max) {
				max = Math.abs(elem[i][k]);
				imax = i;
			}
		}
		if(k!=imax) {swapRow(k,imax);}
	}
	
	private void pivot(int[] index,int k) {
		int imax = k;
		double max = elem[k][k];
		for(int i=k+1;i<row;i++) {
			if(Math.abs(elem[i][k]) > max) {
				max = Math.abs(elem[i][k]);
				imax = i;
			}
		}
		if(k!=imax) {swapRow(k,imax);}
		int temp = index[imax];
		index[imax] = index[k];
		index[k] = temp;
	}
	void linearSolve() {
		if(row > column){
			System.out.println("Array2D_LUDecompose : matrix is not a rectangular matrix.");
		}
		for(int k=0;k<row;k++){
			pivot(k);
			for(int i=k+1;i<row;i++){
				elem[i][k] /= elem[k][k];
				for(int j=k+1;j<column;j++){
					elem[i][j] -= (elem[i][k])*(elem[k][j]);
				}
			}
		}
		/*backward substitution*/
		for(int i=row-1;i>=0;i--){
			for(int j=row;j<column;j++){
				for(int k=i+1;k<row;k++){
					elem[i][j] -= (elem[i][k])*(elem[k][j]);
				}
				elem[i][j] /= (elem[i][i]);
			}
		}
	}
	void linearSolve(int[] index) {
		if(row > column){
			System.out.println("Array2D_LUDecompose : matrix is not a rectangular matrix.");
		}
		for(int i=0;i<row;i++){index[i] = i;}
		/*forward elimination*/
		for(int k=0;k<row;k++){
			pivot(index,k);
			for(int i=k+1;i<row;i++){
				elem[i][k] /= elem[k][k];
				for(int j=k+1;j<column;j++){
					elem[i][j] -= (elem[i][k])*(elem[k][j]);
				}
			}
		}
		/*backward substitution*/
		for(int i=row-1;i>=0;i--){
			for(int j=row;j<column;j++){
				for(int k=i+1;k<row;k++){
					elem[i][j] -= (elem[i][k])*(elem[k][j]);
				}
				elem[i][j] /= (elem[i][i]);
			}
		}
	}
	int indexLowerBound(double min, int c) {
		for(int i=0;i<elem.length;i++) {
			if(min<elem[i][c]) {
				return i;
			}
		}
		return elem.length-1;
	}	
	int indexUpperBound(double max, int c) {
		for(int i=elem.length-1;i>=0;i--) {
			if(elem[i][c]<max) {
				return i;
			}
		}
		return 0;
	}
	Array2D linefit() {
		return polyfit(this,0,elem.length,2);
	}
	Array2D linefit(double min, double max) {
		return polyfit(this,indexLowerBound(min,0),indexUpperBound(max,0),2);
	}
	Array2D polyfit(double min, double max, int degree) {
		return polyfit(this,indexLowerBound(min,0),indexUpperBound(max,0),degree);
	}
	Array2D polyfit(int degree) {
		return polyfit(this,0,elem.length,degree);
	}
	static Array2D polyfit(Array2D array, int imin, int imax, int degree){
		Array2D A = new Array2D(degree,degree+array.column);
		for(int i=0;i<degree;i++) {
			for(int j=0;j<degree;j++) {
				A.elem[i][j] = 0;
				for(int k=imin;k<=imax;k++) {
					A.elem[i][j] += Math.pow(array.elem[k][0],i+j);
				}
			}
		}
		for(int j=0;j<array.column;j++) {
			for(int i=0;i<degree;i++) {
				A.elem[i][degree+j]=0;
				for(int k=imin;k<=imax;k++) {
					A.elem[i][degree+j] += Math.pow(array.elem[k][0],i)*(array.elem[k][j]);
				}
			}
		}
		A.linearSolve();
		Array2D b = new Array2D(degree,array.column);
		for(int i=0;i<degree;i++) {
			for(int j=0;j<array.column;j++) {
				b.elem[i][j] = A.elem[i][degree+j];
			}
		}
		return b;
	}
}

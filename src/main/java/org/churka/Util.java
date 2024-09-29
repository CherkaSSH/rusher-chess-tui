package org.churka;

public class Util {
    public static class Coord{
        public Coord(int x,int y) {
             this.x=x;
             this.y=y;
        };
        private int x,y;

        public void setCoords(int[] coords){
            this.x=coords[0];
            this.y=coords[1];
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public void setX(int x) {
            this.x = x;
        }

        public void setY(int y) {
            this.y = y;
        }
    }
}

package me.withbeth.mij.concurrency.async.syncAndAsync;

record PairResult(int left, int right) {
    static class Builder {
        private int left;
        private int right;

        Builder left(int val) {
            left = val;
            return this;
        }

        Builder right(int val) {
            right = val;
            return this;
        }

        PairResult build() {
            return new PairResult(left, right);
        }

        @Override
        public String toString() {
            return "Builder{" +
                    "left=" + left +
                    ", right=" + right +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "{" +
                "left=" + left +
                ", right=" + right +
                '}';
    }
}

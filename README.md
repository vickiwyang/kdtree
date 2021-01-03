# kdtree

Creates a symbol-table data type whose keys are two-dimensional points to support efficient range search and nearest neighbor search (NNS).

**PointST.java** is a brute-force range search and NNS implementation that uses a [red-black binary search tree](https://en.wikipedia.org/wiki/Red%E2%80%93black_tree).

**KdTreeST.java** implements range search and NNS using a [2-d tree](https://en.wikipedia.org/wiki/K-d_tree). This implementation is much faster than `PointST`: in a trial run, `KdTreeST` made over 317,000 calls per second to `nearest()`, compared to just 8 NNS calls per second for `PointST`.

--

*This assignment was completed as part of COS 226 at Princeton University in Fall 2020.*

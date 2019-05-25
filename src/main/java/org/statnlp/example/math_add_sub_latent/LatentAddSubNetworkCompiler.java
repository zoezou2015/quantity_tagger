package org.statnlp.example.math_add_sub_latent;

import java.util.ArrayList;
import java.util.HashMap;

import org.statnlp.commons.types.Instance;
import org.statnlp.commons.types.Sentence;
import org.statnlp.example.base.BaseNetwork;
import org.statnlp.example.base.BaseNetwork.NetworkBuilder;
import org.statnlp.hypergraph.LocalNetworkParam;
import org.statnlp.hypergraph.Network;
import org.statnlp.hypergraph.NetworkCompiler;
import org.statnlp.hypergraph.NetworkIDMapper;

public class LatentAddSubNetworkCompiler extends NetworkCompiler {
	private static final long serialVersionUID = 7106577709877770005L;

	protected ArrayList<String> _signs;
	protected HashMap<String, Integer> _sign2id;
	private boolean debug = true;

	protected enum NodeType {
		leaf, num, root
	};

	protected enum SubNodeType {
		before, number, after
	};

	protected enum SignType {
		zero, minus, plus
	};

	public LatentAddSubNetworkCompiler(ArrayList<String> signs) {
		this._signs = signs;
		_sign2id = new HashMap<>();
		for (String s : signs) {
			_sign2id.put(s, _sign2id.size());
		}

		NetworkIDMapper.setCapacity(new int[] { 100, 10, 3, 3 });
	}

	@Override
	public BaseNetwork compileLabeled(int networkId, Instance inst, LocalNetworkParam param) {

		NetworkBuilder<BaseNetwork> builder = NetworkBuilder.builder();
		LatentAddSubInstance mathInst = (LatentAddSubInstance) inst;
		Sentence text = mathInst.getProblemText();
		ArrayList<Integer> numberPos = mathInst.getNumberPos();
		ArrayList<String> signs = mathInst.getOutput();
		ArrayList<String> numbers = mathInst.getNumbers();

		// System.out.println(numberPos);
		// System.out.println(signs);
		// System.out.println(numbers);
		long leaf = this.toNode_Leaf();
		builder.addNode(leaf);
		ArrayList<Long> children = new ArrayList<Long>() {
			{
				add(leaf);
			}
		};
		ArrayList<Long> current;
		// for (int num = 0; num < numberPos.size(); num++) {
		// if (num == numberPos.size() - 1)
		// break; // the last pos is for unknown variabel, we consider as latent
		// variable
		// int pos = numberPos.get(num);
		//
		// }

		for (int pos = 0; pos < text.length(); pos++) {
			current = new ArrayList<>();
			// at this position, this is a number but it is not "X"
			if (numberPos.indexOf(pos) != -1) {
				String sign = signs.get(numberPos.indexOf(pos));
				int signId = _sign2id.get(sign);
				long numNode = this.toNode_Num(pos, signId);
				builder.addNode(numNode);
				for (long child : children) {
					builder.addEdge(numNode, new long[] { child });
				}
				current.add(numNode);
			} else {
				String[] rangeSings = getRangeSign(numberPos, signs, pos);
				if (rangeSings[0].equals("") && rangeSings[1].equals("")) {
					// do nothing
				} else if (rangeSings[0].equals("") && !rangeSings[1].equals("")) {
					int signId1 = _sign2id.get(rangeSings[1]);
					long node_b = this.toNode_B(pos, signId1);
					builder.addNode(node_b);
					for (long child : children) {
						// N -> A or A -> A
						builder.addEdge(node_b, new long[] { child });
					}
					current.add(node_b);
				} else if (!rangeSings[0].equals("") && rangeSings[1].equals("")) {
					int signId0 = _sign2id.get(rangeSings[0]);
					long node_a = this.toNode_A(pos, signId0);
					builder.addNode(node_a);
					for (long child : children) {
						// B -> B or B -> N
						builder.addEdge(node_a, new long[] { child });
					}
					current.add(node_a);
				} else {
					// A -> A or A -> B or N -> B or A -> N
					int signId0 = _sign2id.get(rangeSings[0]);
					int signId1 = _sign2id.get(rangeSings[1]);
					long node_a = this.toNode_A(pos, signId0);
					long node_b = this.toNode_B(pos, signId1);
					builder.addNode(node_a);
					builder.addNode(node_b);
					for (long child : children) {
						int[] child_arr = NetworkIDMapper.toHybridNodeArray(child);
						// child is a leaf node
						if (child_arr[2] == NodeType.leaf.ordinal()) {
							builder.addEdge(node_b, new long[] { child });
							builder.addEdge(node_a, new long[] { child });
						}
						// child is a after node
						else if (child_arr[2] == NodeType.num.ordinal()
								&& child_arr[3] == SubNodeType.after.ordinal()) {
							// A -> A
							builder.addEdge(node_a, new long[] { child });
							builder.addEdge(node_b, new long[] { child });
						}
						// child is a before node
						else if (child_arr[2] == NodeType.num.ordinal()
								&& child_arr[3] == SubNodeType.before.ordinal()) {
							// B -> B
							builder.addEdge(node_b, new long[] { child });
						}
						// child is a number node
						else if (child_arr[2] == NodeType.num.ordinal()
								&& child_arr[3] == SubNodeType.number.ordinal()) {
							// B -> N
							builder.addEdge(node_b, new long[] { child });
							// A -> N
							builder.addEdge(node_a, new long[] { child });
						}
					}
					current.add(node_a);
					current.add(node_b);
				}

			}
			children.clear();
			children.addAll(current);
		}

		long root = this.toNode_Root(text.length());
		builder.addNode(root);
		for (long child : children) {
			builder.addEdge(root, new long[] { child });
		}
		BaseNetwork network = builder.build(networkId, mathInst, param, this);
		if (debug) {
			BaseNetwork unlabelNetwork = compileUnlabeled(networkId, mathInst, param);
			if (!unlabelNetwork.contains(network)) {
				System.err.println("Problem id: " + mathInst.getProblemId());
				throw new RuntimeException("Unlable network should contain label network");
			}
		}
		return network;
	}

	@Override
	public BaseNetwork compileUnlabeled(int networkId, Instance inst, LocalNetworkParam param) {

		NetworkBuilder<BaseNetwork> builder = NetworkBuilder.builder();
		LatentAddSubInstance mathInst = (LatentAddSubInstance) inst;
		Sentence text = mathInst.getProblemText();
		ArrayList<Integer> numberPos = mathInst.getNumberPos();
		ArrayList<String> numbers = mathInst.getNumbers();
		long leaf = this.toNode_Leaf();
		builder.addNode(leaf);
		ArrayList<Long> children = new ArrayList<Long>() {
			{
				add(leaf);
			}
		};
		ArrayList<Long> current;

		for (int pos = 0; pos < text.length(); pos++) {
			current = new ArrayList<>();
			// at this position, this is a number but it is not "X"
			if (numberPos.indexOf(pos) != -1) {
				for (String s : this._signs) {
					if (numberPos.indexOf(pos) == (numberPos.size() - 1) && s.equals("zero")
							&& numbers.get(numbers.size() - 1).equals("X")) {
						continue;
					}
					if (numberPos.size() == 3 && s.equals("zero") && !LatentAddSubConfig.isMath23K) {
						continue;
					}
					int signId = _sign2id.get(s);
					long numNode = this.toNode_Num(pos, signId);
					builder.addNode(numNode);
					for (long child : children) {
						int[] child_arr = NetworkIDMapper.toHybridNodeArray(child);
						if (child_arr[2] == NodeType.leaf.ordinal()) {
							builder.addEdge(numNode, new long[] { child });
						} else if (child_arr[2] == NodeType.num.ordinal()
								&& child_arr[3] == SubNodeType.after.ordinal()) {
							// A -> N
							builder.addEdge(numNode, new long[] { child });
						}
						// child is a before node
						else if (child_arr[2] == NodeType.num.ordinal()
								&& child_arr[3] == SubNodeType.before.ordinal()) {
							// B -> N
							if (signId == child_arr[1])
								builder.addEdge(numNode, new long[] { child });
						}
						// child is a number node
						else if (child_arr[2] == NodeType.num.ordinal()
								&& child_arr[3] == SubNodeType.number.ordinal()) {
							// N -> N
							builder.addEdge(numNode, new long[] { child });
						}
					}
					current.add(numNode);
				}
			} else {
				int[] ranges = getRange(numberPos, pos);
				if (ranges[0] == -1 && ranges[1] == -1) {
					// do nothing
				} else if (ranges[0] == -1 && ranges[1] != -1) {
					for (String s : this._signs) {
						if (numberPos.size() == 3 && s.equals("zero") && !LatentAddSubConfig.isMath23K) {
							continue;
						}
						int signId = _sign2id.get(s);
						long node_b = this.toNode_B(pos, signId);
						builder.addNode(node_b);
						for (long child : children) {
							// B -> B or B -> N
							int[] child_arr = NetworkIDMapper.toHybridNodeArray(child);
							if (child_arr[2] == NodeType.leaf.ordinal()) {
								builder.addEdge(node_b, new long[] { child });
							} else if (child_arr[2] == NodeType.num.ordinal()
									&& child_arr[3] == SubNodeType.after.ordinal()) {
								// A -> N
								builder.addEdge(node_b, new long[] { child });
							}
							// child is a before node
							else if (child_arr[2] == NodeType.num.ordinal()
									&& child_arr[3] == SubNodeType.before.ordinal()) {
								// B -> N
								if (signId == child_arr[1])
									builder.addEdge(node_b, new long[] { child });
							}
							// child is a number node
							else if (child_arr[2] == NodeType.num.ordinal()
									&& child_arr[3] == SubNodeType.number.ordinal()) {
								builder.addEdge(node_b, new long[] { child });
							}
						}
						current.add(node_b);
					}
				} else if (ranges[0] != -1 && ranges[1] == -1) {
					for (String s : this._signs) {
						if (s.equals("zero") && numbers.get(numbers.size() - 1).equals("X")) {
							continue;
						}
						if (numberPos.size() == 3 && s.equals("zero") && !LatentAddSubConfig.isMath23K) {
							continue;
						}
						int signId = _sign2id.get(s);
						long node_a = this.toNode_A(pos, signId);
						builder.addNode(node_a);
						for (long child : children) {
							int[] child_arr = NetworkIDMapper.toHybridNodeArray(child);
							if (child_arr[2] == NodeType.leaf.ordinal()) {
								builder.addEdge(node_a, new long[] { child });
							} else if (child_arr[2] == NodeType.num.ordinal()
									&& child_arr[3] == SubNodeType.after.ordinal()) {
								if (signId == child_arr[1])
									builder.addEdge(node_a, new long[] { child });
							}
							// child is a before node
							else if (child_arr[2] == NodeType.num.ordinal()
									&& child_arr[3] == SubNodeType.before.ordinal()) {
								// B -> A

								// builder.addEdge(node_a, new long[] { child });
							}
							// child is a number node
							else if (child_arr[2] == NodeType.num.ordinal()
									&& child_arr[3] == SubNodeType.number.ordinal()) {
								if (signId == child_arr[1])
									builder.addEdge(node_a, new long[] { child });
							}
						}
						current.add(node_a);
					}
				} else if (ranges[0] != -1 && ranges[1] != -1) {
					// A -> A or A -> B or N -> B or A -> N
					for (String s : this._signs) {
						if (numberPos.size() == 3 && s.equals("zero") && !LatentAddSubConfig.isMath23K) {
							continue;
						}
						int signId = _sign2id.get(s);
						long node_a = this.toNode_A(pos, signId);
						long node_b = this.toNode_B(pos, signId);
						builder.addNode(node_a);
						builder.addNode(node_b);
						for (long child : children) {
							int[] child_arr = NetworkIDMapper.toHybridNodeArray(child);
							// child is a leaf node
							if (child_arr[2] == NodeType.leaf.ordinal()) {
								builder.addEdge(node_b, new long[] { child });
								builder.addEdge(node_a, new long[] { child });
							}
							// child is a after node
							else if (child_arr[2] == NodeType.num.ordinal()
									&& child_arr[3] == SubNodeType.after.ordinal()) {
								// A -> A
								builder.addEdge(node_b, new long[] { child });
								// A -> N
								if (signId == child_arr[1])
									builder.addEdge(node_a, new long[] { child });
							}
							// child is a before node
							else if (child_arr[2] == NodeType.num.ordinal()
									&& child_arr[3] == SubNodeType.before.ordinal()) {
								// B -> B
								if (signId == child_arr[1])
									builder.addEdge(node_b, new long[] { child });
							}
							// child is a number node
							else if (child_arr[2] == NodeType.num.ordinal()
									&& child_arr[3] == SubNodeType.number.ordinal()) {
								// B -> N
								builder.addEdge(node_b, new long[] { child });
								// A -> N
								if (signId == child_arr[1])
									builder.addEdge(node_a, new long[] { child });
							}
						}
						current.add(node_a);
						current.add(node_b);
					}
				}
			}
			children.clear();
			children.addAll(current);
		}

		long root = this.toNode_Root(text.length());
		builder.addNode(root);
		for (long child : children) {
			builder.addEdge(root, new long[] { child });
		}

		BaseNetwork network = builder.build(networkId, mathInst, param, this);

		return network;

	}

	@Override
	public LatentAddSubInstance decompile(Network network) {

		LatentAddSubInstance instance = (LatentAddSubInstance) network.getInstance();
		int size = instance.size();
		long root = network.getRoot();
		int currIdx = network.getNodeIndex(root);
		ArrayList<String> prediction = new ArrayList<>();
		ArrayList<String> span = new ArrayList<>();
		for (int i = 0; i < size; i++) {
			int[] children = network.getMaxPath(currIdx);
			int child = children[0];
			int[] child_arr = network.getNodeArray(child);
			if (child_arr[2] == NodeType.num.ordinal() && child_arr[3] == SubNodeType.number.ordinal()) {
				prediction.add(0, this._signs.get(child_arr[1]));
				span.add(0, "N");
			} else if (child_arr[2] == NodeType.num.ordinal() && child_arr[3] == SubNodeType.before.ordinal()) {
				span.add(0, "B");
			} else if (child_arr[2] == NodeType.num.ordinal() && child_arr[3] == SubNodeType.after.ordinal()) {
				span.add(0, "A");
			}
			currIdx = child;
		}
		instance.setPrediction(prediction);
		instance.setSpan(span);
		return instance;
	}

	private long toNode_Root(int sentLength) {
		return this.toNode(sentLength, this._signs.size(), NodeType.root.ordinal(), 0);
	}

	private long toNode_Leaf() {
		return this.toNode(0, 0, NodeType.leaf.ordinal(), 0);
	}

	private long toNode_B(int pos, int signId) {
		return this.toNode(pos, signId, NodeType.num.ordinal(), SubNodeType.before.ordinal());
	}

	private long toNode_Num(int pos, int signId) {
		return this.toNode(pos, signId, NodeType.num.ordinal(), SubNodeType.number.ordinal());
	}

	private long toNode_A(int pos, int signId) {
		return this.toNode(pos, signId, NodeType.num.ordinal(), SubNodeType.after.ordinal());
	}

	private long toNode(int pos, int signId, int NodeTypeId, int subNodeTypeId) {
		return NetworkIDMapper.toHybridNodeID(new int[] { pos, signId, NodeTypeId, subNodeTypeId });
	}

	private int[] getRange(ArrayList<Integer> numberPos, int pos) {
		int[] range = new int[2];
		range[0] = -1;
		range[1] = -1;
		for (int i = 0; i < numberPos.size(); i++) {
			if (numberPos.get(i) < pos) {
				range[0] = numberPos.get(i);
			}
		}
		for (int i = numberPos.size() - 1; i >= 0; i--) {
			if (numberPos.get(i) > pos) {
				range[1] = numberPos.get(i);
			}
		}

		return range;
	}

	private String[] getRangeSign(ArrayList<Integer> numberPos, ArrayList<String> signs, int pos) {
		String[] rangeSigns = new String[2];
		int[] ranges = getRange(numberPos, pos);

		if (ranges[0] == -1 && ranges[1] == -1) {
			rangeSigns[0] = "";
			rangeSigns[1] = "";
		} else if (ranges[1] == -1) {
			rangeSigns[0] = signs.get(signs.size() - 1);
			rangeSigns[1] = "";
		} else if (ranges[0] == -1) {
			rangeSigns[0] = "";
			rangeSigns[1] = signs.get(0);
		} else {
			rangeSigns[0] = signs.get(numberPos.indexOf(ranges[0]));
			rangeSigns[1] = signs.get(numberPos.indexOf(ranges[1]));
		}
		// System.out.println(ranges[0] + " " + ranges[1]);
		// System.out.println(rangeSigns[0] + " " + rangeSigns[1]);
		return rangeSigns;
	}

}

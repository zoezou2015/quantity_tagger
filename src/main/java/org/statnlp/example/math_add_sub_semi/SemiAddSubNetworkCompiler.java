package org.statnlp.example.math_add_sub_semi;

import java.util.ArrayList;
import java.util.HashMap;

import org.statnlp.commons.types.Instance;
import org.statnlp.commons.types.Sentence;
import org.statnlp.example.base.BaseNetwork;
import org.statnlp.example.base.BaseNetwork.NetworkBuilder;
import org.statnlp.example.math_add_sub_latent.LatentAddSubInstance;
import org.statnlp.hypergraph.LocalNetworkParam;
import org.statnlp.hypergraph.Network;
import org.statnlp.hypergraph.NetworkCompiler;
import org.statnlp.hypergraph.NetworkIDMapper;

public class SemiAddSubNetworkCompiler extends NetworkCompiler {

	private static final long serialVersionUID = 1738675156641181707L;
	protected ArrayList<String> _signs;
	protected HashMap<String, Integer> _sign2id;
	private boolean debug = true;
	private static int MAX_SIZE = 150;

	protected enum NodeType {
		leaf, num, root
	};

	protected enum SubNodeType {
		before, number, after
	};

	protected enum SignType {
		zero, minus, plus
	};

	public SemiAddSubNetworkCompiler(ArrayList<String> signs) {
		this._signs = signs;
		_sign2id = new HashMap<>();
		for (String s : signs) {
			_sign2id.put(s, _sign2id.size());
		}

		NetworkIDMapper.setCapacity(new int[] { 200, 200, 10, 3, 3 });
	}

	@Override
	public BaseNetwork compileLabeled(int networkId, Instance inst, LocalNetworkParam param) {

		NetworkBuilder<BaseNetwork> builder = NetworkBuilder.builder();
		LatentAddSubInstance mathInst = (LatentAddSubInstance) inst;
		Sentence text = mathInst.getProblemText();
		ArrayList<Integer> partTextPos = mathInst.getPartTextPos();
		ArrayList<Integer> numberPos = mathInst.getNumberPos();
		ArrayList<String> signs = mathInst.getOutput();
		ArrayList<String> numbers = mathInst.getNumbers();
		int halfWindowSize = SemiAddSubConfig.halfWindowSize;
		int wholeSentenceLength = text.length();
		HashMap<Integer, ArrayList<Integer>> num2window = new HashMap<>();
		addNumWindow(halfWindowSize, wholeSentenceLength, numberPos, num2window);
		long leaf = this.toNode_Leaf();
		builder.addNode(leaf);
		ArrayList<Long> children = new ArrayList<Long>() {
			{
				add(leaf);
			}
		};

		for (int numPos : numberPos) {
			if (numberPos.indexOf(numPos) == -1) {
				throw new RuntimeException();
			}
			String sign = signs.get(numberPos.indexOf(numPos));
			int signId = _sign2id.get(sign);
			ArrayList<Integer> window = num2window.get(numPos);
			ArrayList<Long> beforeNodes = new ArrayList<>();
			ArrayList<Long> afterNodes = new ArrayList<>();
			for (int w : window) {
				if (w < numPos) {
					long b_node = this.toNode_B(numPos, w, signId);
					builder.addNode(b_node);
					beforeNodes.add(b_node);
				} else if (w == numPos) {
					long b_node = this.toNode_B(numPos, w, signId);
					long a_node = this.toNode_A(numPos, w, signId);
					builder.addNode(b_node);
					builder.addNode(a_node);
					beforeNodes.add(b_node);
					afterNodes.add(a_node);
				} else if (w > numPos) {
					long a_node = this.toNode_A(numPos, w, signId);
					builder.addNode(a_node);
					afterNodes.add(a_node);
				}
			}
			for (long b_n : beforeNodes) {

				for (long a_n : afterNodes) {
					builder.addEdge(a_n, new long[] { b_n });
				}
				for (long c_n : children) {
					builder.addEdge(b_n, new long[] { c_n });
				}
			}
			children.clear();
			children.addAll(afterNodes);
		}
		long root = this.toNode_Root(MAX_SIZE);
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
		ArrayList<Integer> partTextPos = mathInst.getPartTextPos();
		ArrayList<Integer> numberPos = mathInst.getNumberPos();
		ArrayList<String> numbers = mathInst.getNumbers();
		int halfWindowSize = SemiAddSubConfig.halfWindowSize;
		int wholeSentenceLength = text.length();
		HashMap<Integer, ArrayList<Integer>> num2window = new HashMap<>();
		addNumWindow(halfWindowSize, wholeSentenceLength, numberPos, num2window);

		long leaf = this.toNode_Leaf();
		builder.addNode(leaf);
		ArrayList<Long> children = new ArrayList<Long>() {
			{
				add(leaf);
			}
		};
		ArrayList<Long> current;

		for (int numPos : numberPos) {
			if (numberPos.indexOf(numPos) == -1) {
				throw new RuntimeException();
			}
			ArrayList<Integer> window = num2window.get(numPos);
			ArrayList<Long> beforeNodes = new ArrayList<>();
			ArrayList<Long> afterNodes = new ArrayList<>();
			for (String s : this._signs) {
				if (numberPos.indexOf(numPos) == (numberPos.size() - 1) && s.equals("zero")
						&& numbers.get(numbers.size() - 1).equals("X")) {
					continue;
				}
				if (numberPos.size() == 3 && s.equals("zero") && !SemiAddSubConfig.isMath23K) {
					continue;
				}
				int signId = _sign2id.get(s);
				for (int w : window) {
					if (w < numPos) {
						long b_node = this.toNode_B(numPos, w, signId);
						builder.addNode(b_node);
						beforeNodes.add(b_node);
					} else if (w == numPos) {
						long b_node = this.toNode_B(numPos, w, signId);
						long a_node = this.toNode_A(numPos, w, signId);
						builder.addNode(b_node);
						builder.addNode(a_node);
						beforeNodes.add(b_node);
						afterNodes.add(a_node);
					} else if (w > numPos) {
						long a_node = this.toNode_A(numPos, w, signId);
						builder.addNode(a_node);
						afterNodes.add(a_node);
					}
				}
			}
			for (long b_n : beforeNodes) {
				int[] b_n_arr = NetworkIDMapper.toHybridNodeArray(b_n);
				int b_sign = b_n_arr[2];
				for (long a_n : afterNodes) {
					int[] a_n_arr = NetworkIDMapper.toHybridNodeArray(a_n);
					int a_sign = a_n_arr[2];
					if (b_sign == a_sign)
						builder.addEdge(a_n, new long[] { b_n });
				}
				for (long c_n : children) {
					builder.addEdge(b_n, new long[] { c_n });
				}
			}

			children.clear();
			children.addAll(afterNodes);

		}
		long root = this.toNode_Root(MAX_SIZE);
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
		// int size = instance.getPartTextPos().size();
		ArrayList<Integer> numberPos = instance.getNumberPos();
		int size = (SemiAddSubConfig.halfWindowSize * 2 + 1) * numberPos.size() + 1;
		long root = network.getRoot();
		int currIdx = network.getNodeIndex(root);
		ArrayList<String> prediction = new ArrayList<>(numberPos.size());
		for (int k = 0; k < numberPos.size(); k++)
			prediction.add("-1");
		ArrayList<String> span = new ArrayList<>();
		for (int i = 0; i < instance.getProblemText().length(); i++) {
			int[] children = network.getMaxPath(currIdx);
			int child = children[0];
			int[] child_arr = network.getNodeArray(child);
			if (child_arr[3] == NodeType.num.ordinal() && child_arr[4] == SubNodeType.before.ordinal()) {
				// int numPosition = numberPos.indexOf(child_arr[0]);
				// prediction.set(numPosition, this._signs.get(child_arr[2]));
				span.add(0, "B");
			} else if (child_arr[3] == NodeType.num.ordinal() && child_arr[4] == SubNodeType.after.ordinal()) {
				int numPosition = numberPos.indexOf(child_arr[0]);
				prediction.set(numPosition, this._signs.get(child_arr[2]));
				span.add(0, "A");

			} else if (child_arr[3] == NodeType.leaf.ordinal()) {
				break;
			}
			currIdx = child;
		}
		instance.setPrediction(prediction);
		instance.setSpan(span);
		return instance;
	}

	private void addNumWindow(int halfWindowSize, int wholeSentenceLength, ArrayList<Integer> numberPos,
			HashMap<Integer, ArrayList<Integer>> num2window) {

		for (int num : numberPos) {
			if (!num2window.keySet().contains(num)) {
				num2window.put(num, new ArrayList<>());
			}
			for (int w = num - halfWindowSize; w <= num + halfWindowSize; w++) {
				if (w >= 0 && w < wholeSentenceLength && !num2window.get(num).contains(w)) {
					num2window.get(num).add(w);
				}

			}

		}
	}

	private long toNode_Root(int sentLength) {
		return this.toNode(sentLength, sentLength, this._signs.size(), NodeType.root.ordinal(), 0);
	}

	private long toNode_Leaf() {
		return this.toNode(0, 0, 0, NodeType.leaf.ordinal(), 0);
	}

	private long toNode_B(int numPos, int pos, int signId) {
		return this.toNode(numPos, pos, signId, NodeType.num.ordinal(), SubNodeType.before.ordinal());
	}

	private long toNode_Num(int numPos, int pos, int signId) {
		return this.toNode(numPos, pos, signId, NodeType.num.ordinal(), SubNodeType.number.ordinal());
	}

	private long toNode_A(int numPos, int pos, int signId) {
		return this.toNode(numPos, pos, signId, NodeType.num.ordinal(), SubNodeType.after.ordinal());
	}

	private long toNode(int numPos, int pos, int signId, int NodeTypeId, int subNodeTypeId) {
		return NetworkIDMapper.toHybridNodeID(new int[] { numPos, pos, signId, NodeTypeId, subNodeTypeId });
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

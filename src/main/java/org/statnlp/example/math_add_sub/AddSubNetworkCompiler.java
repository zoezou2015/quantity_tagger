package org.statnlp.example.math_add_sub;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.statnlp.commons.types.Instance;
import org.statnlp.example.base.BaseNetwork;
import org.statnlp.example.base.BaseNetwork.NetworkBuilder;
import org.statnlp.hypergraph.LocalNetworkParam;
import org.statnlp.hypergraph.Network;
import org.statnlp.hypergraph.NetworkCompiler;
import org.statnlp.hypergraph.NetworkIDMapper;

public class AddSubNetworkCompiler extends NetworkCompiler {

	public HashMap<String, Integer> sign2id;
	public ArrayList<String> signs;

	private static final long serialVersionUID = -222524356242836251L;
	protected final boolean DEBUG = true;

	protected enum NodeType {
		leaf, number, root
	};

	protected enum SignType {
		zero, minus, plus
	};

	static {
		NetworkIDMapper.setCapacity(new int[] { 10, 4, 3 });
	}

	public AddSubNetworkCompiler(ArrayList<String> signs) {
		this.signs = signs;

		this.sign2id = new HashMap<>(signs.size());
		for (int i = 0; i < this.signs.size(); i++) {
			this.sign2id.put(this.signs.get(i), i);
		}

	}

	@Override
	public BaseNetwork compileLabeled(int networkId, Instance inst, LocalNetworkParam param) {
		NetworkBuilder<BaseNetwork> builder = NetworkBuilder.builder();
		AddSubInstance mathInst = (AddSubInstance) inst;
		long leaf = toNode_Leaf();
		builder.addNode(leaf);
		ArrayList<String> output = mathInst.getOutput();
		ArrayList<String> input_numbers = mathInst.getInput();
		long[] children = new long[] { leaf };
		for (int i = 0; i < input_numbers.size(); i++) {
			String sign = output.get(i);
			int sign_id = this.sign2id.get(sign);
			long numNode = this.toNode_num(i, sign_id);
			builder.addNode(numNode);
			builder.addEdge(numNode, children);
			children = new long[] { numNode };
		}
		long root = this.toNode_root(input_numbers.size());
		builder.addNode(root);
		builder.addEdge(root, children);
		BaseNetwork labeledNetwork = builder.build(networkId, inst, param, this);
		if (DEBUG) {
			BaseNetwork unlabeledNetwork = this.compileUnlabeled(networkId, inst, param);
			if (!unlabeledNetwork.contains(labeledNetwork)) {
			}
		}
		return labeledNetwork;
	}

	@Override
	public BaseNetwork compileUnlabeled(int networkId, Instance inst, LocalNetworkParam param) {
		NetworkBuilder<BaseNetwork> builder = NetworkBuilder.builder();
		AddSubInstance mathInst = (AddSubInstance) inst;
		long leaf = toNode_Leaf();
		builder.addNode(leaf);
		ArrayList<String> input_numbers = mathInst.getInput();
		ArrayList<Long> children = new ArrayList<Long>() {
			{
				add(leaf);
			}
		};
		ArrayList<Long> current;
		for (int i = 0; i < input_numbers.size(); i++) {
			current = new ArrayList<>();
			for (int s = 0; s < this.signs.size(); s++) {
				if (input_numbers.size() == 3 && this.signs.get(s).equals("zero") && !AddSubConfig.isMath23K) {
					continue;
				}

				long numNode = this.toNode_num(i, s);
				builder.addNode(numNode);
				for (long child : children) {
					if (input_numbers.size() == 3 && this.signs.get(s).equals("minus")) {
						int[] child_arr = NetworkIDMapper.toHybridNodeArray(child);
						// if (this.signs.get(child_arr[1]).equals("minus"))
						// continue;
					}
					builder.addEdge(numNode, new long[] { child });
				}
				current.add(numNode);
			}
			children.clear();
			children.addAll(current);
		}
		long root = this.toNode_root(input_numbers.size());
		builder.addNode(root);
		for (long child : children)
			builder.addEdge(root, new long[] { child });
		BaseNetwork unlabeledNetwork = builder.build(networkId, inst, param, this);
		return unlabeledNetwork;
	}

	@Override
	public AddSubInstance decompile(Network network) {
		BaseNetwork unlabeledNetwork = (BaseNetwork) network;
		AddSubInstance inst = (AddSubInstance) network.getInstance();
		// inst = inst.duplicate();

		int size = inst.size();

		long rootNode = this.toNode_root(size);
		int currIdx = Arrays.binarySearch(unlabeledNetwork.getAllNodes(), rootNode);
		ArrayList<String> prediction = new ArrayList<>();
		for (int i = 0; i < size; i++) {
			int[] children = unlabeledNetwork.getMaxPath(currIdx);
			int child = children[0];
			int[] childArr = unlabeledNetwork.getNodeArray(child);
			prediction.add(0, this.signs.get(childArr[1]));
			currIdx = child;
		}

		inst.setPrediction(prediction);
		// System.out.println("child: " + prediction);
		// System.exit(0);
		return inst;
	}

	protected long toNode_root(int size) {
		return toNode(size - 1, this.signs.size(), NodeType.root);
	}

	protected long toNode_num(int pos, int labelId) {
		return toNode(pos, labelId, NodeType.number);
	}

	protected long toNode_Leaf() {
		return toNode(0, 0, NodeType.leaf);
	}

	protected long toNode(int pos, int labelId, NodeType nodeType) {
		return NetworkIDMapper.toHybridNodeID(new int[] { pos, labelId, nodeType.ordinal() });
	}
}

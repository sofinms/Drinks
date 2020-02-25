package com.coffeedose.ui


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController

import com.coffeedose.R
import com.coffeedose.databinding.FragmentOrderAwaitingBinding
import com.coffeedose.extensions.setBooleanVisibility
import com.coffeedose.viewmodels.OrderAwaitingViewModel
import kotlinx.android.synthetic.main.fragment_order_awaiting.*

/**
 * A simple [Fragment] subclass.
 */
class OrderAwaitingFragment : Fragment() {

    private lateinit var viewModel : OrderAwaitingViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {

        val orderId = OrderAwaitingFragmentArgs.fromBundle(arguments!!).orderId

        viewModel = ViewModelProvider(this,OrderAwaitingViewModel.Factory(requireNotNull(this.activity).application,orderId)).get(OrderAwaitingViewModel::class.java)
        val binding : FragmentOrderAwaitingBinding = DataBindingUtil.inflate(inflater,R.layout.fragment_order_awaiting,container,false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        initToolbar()


        viewModel.order.observe(this, Observer {
            if (it != null)
            {
                val toolbar = (activity as AppCompatActivity)?.supportActionBar
                toolbar?.setTitle("Заказ ${it.orderNumber}")
                tv_order_status.text = "Статус: ${it.statusName}"

                if (it.statusCode.toLowerCase() == "ready"){
                    bv_approve.setBooleanVisibility(true)
                }
            }
        })



        binding.bvApprove.setOnClickListener {
            viewModel.approve()
            findNavController().navigate(OrderAwaitingFragmentDirections.actionOrderAwaitingFragmentToDrinksFragment())
        }


        return binding.root
    }


    private fun initToolbar(){
        val toolbar = (activity as AppCompatActivity)?.supportActionBar
        toolbar?.let {
            it.setDisplayHomeAsUpEnabled(false)
            it.setDisplayShowHomeEnabled(false)
        }
    }
}

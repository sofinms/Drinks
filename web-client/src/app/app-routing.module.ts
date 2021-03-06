import { NgModule } from '@angular/core';
import { UIRouterModule } from "@uirouter/angular";
import { CartComponent }   from './cart/cart.component';
import { OrderComponent }   from './order/order.component';
import { SigninComponent }   from './signin/signin.component';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { uiRouterConfigFn } from "./router.config.js";

const cart_state = { name: "cart", url: "/cart", component: CartComponent };
const order_state = { name: "order", url: "/order", component: OrderComponent, data: {
      requiresAuth: true
    }, };
const signin_state = { name: "signin", url: "/signin", params: {order_creating_started: false}, component: SigninComponent };

@NgModule({
  declarations: [
    OrderComponent,
    CartComponent,
    SigninComponent
  ],
  imports: [
    UIRouterModule.forRoot({ states: [cart_state, order_state, signin_state], useHash: true, otherwise: '/drinks', config: uiRouterConfigFn }),
    CommonModule,
    FormsModule
  ],
  exports: [UIRouterModule]
})
export class AppRoutingModule { }

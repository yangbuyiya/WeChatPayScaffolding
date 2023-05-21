import {createRouter, createWebHistory} from "vue-router";
// // 引入组件
import Index from '../views/index'
import Orders from '../views/Orders'
import Download from '../views/Download'
import Success from '../views/Success'

const constantRoutes = [
	{
		path: '/',
		component: Index
	},
	{
		path: '/wx/',
		component: Index
	},
	{
		path: '/wx/orders',
		component: Orders
	},
	{
		path: '/wx/download',
		component: Download
	},
	{
		path: '/wx/success',
		component: Success
	}
];

const router = createRouter({
	history: createWebHistory(),
	routes: constantRoutes,
});

export default router;

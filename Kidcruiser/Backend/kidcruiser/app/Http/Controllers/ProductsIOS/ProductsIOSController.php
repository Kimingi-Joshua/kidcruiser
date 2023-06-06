<?php

namespace App\Http\Controllers\ProductsIOS;

use Illuminate\Http\Request;
use App\Http\Controllers\Controller;
use Illuminate\Support\Facades\Auth;
use Illuminate\Support\Facades\Hash;
use Illuminate\Support\Facades\Storage;
use Validator;
use App\Product;
use DB;
use App\Driver;
use App\Setting;
use App\SettingType;
use App\IosProduct;

class ProductsIOSController extends Controller
{
    /* show all products*/
    public function showIndex (Request $request)
    {
        return view('productsIOS.index');
    }
    
    /* get all products based on filter */
    public function filter (Request $request)
    {
        //get products based on search term if any
        $query = IosProduct::query();
        if($request->search) {
            $query->where('name', 'LIKE', '%'.$request->search.'%');
        }
        // sort the obtained products
        if($request->input('orderBy.direction')) {
            $products = $query->orderBy($request->input('orderBy.column'), $request->input('orderBy.direction'))
                    ->paginate($request->input('pagination.per_page'));
        }
        else
        {
            $products = $query->get();
        }
        return ['products' => $products];
    }
    /* get specific product */
    public function show ($product)
    {
        return ['product' => IosProduct::findOrFail($product)];
    }
    /* get all products */
    public function all ()
    {
        return [
            'products' => IosProduct::get(),
        ];
    }
    /* delete a specific product */
    public function destroy($product)
    {
        $product = IosProduct::find($product);
        $product->forceDelete();
    }
    /* create a new product */
    public function store (Request $request)
    {
        // make nice names for validation
        $niceNames = [
        ]; 
        $this->validate($request, [
            'name' => 'required|string|unique:ios_products,name,'.$request->id,
            'price' => 'required|numeric|min:0',
        ], [], $niceNames);
        // create local product on database
        $product = IosProduct::create([
            'name' => $request->name,
            'price' => $request->price,
        ]);
        return $product;
    }
    /* update a specific product's data. Note that the method is allowed only for Free product*/
    public function update (Request $request)
    {
        $niceNames = [
        ]; 
        $this->validate($request, [
            'name' => 'required|string|unique:ios_products,name,'.$request->id,
            'price' => 'required|numeric|min:0',
        ], [], $niceNames);

        //update product
        // get the local product to be updated
        $product = IosProduct::find($request->id);
        if($product)
        {
            $product->name = $request->name;
            $product->price = $request->price;
            $product->save();
            return $product; 
        }
        else {
            return response()->json(['errors' => ['Product'=> ['product can not be updated']]], 422);
        }
    }

}
